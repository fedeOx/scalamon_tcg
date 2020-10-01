package model.ia

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

import common.TurnOwner.TurnOwner
import common.{Observer, TurnOwner}
import model.core.{GameManager, TurnManager}
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, EndGame}
import model.exception.InvalidOperationException
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Board, EnergyType}


case class Ia() extends Thread with Observer {

  private val eventQueue: BlockingQueue[Event] = new ArrayBlockingQueue[Event](20)

  TurnManager.addObserver(this)
  GameManager.addObserver(this)
  private var opponentBoard: Board = _
  private var playerBoard: Board = _
  private var myHand: Seq[Card] = _
  private var turn: TurnOwner = _
  private var isCancelled: Boolean = false


  private def placeCards(hand: Seq[Card]): Unit = {
    val basePokemons: Seq[PokemonCard] = hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName == "").asInstanceOf[Seq[PokemonCard]]
    val pokemonWithMaxWeight = basePokemons.flatMap(pkm => createWeightedCard(pkm, calculateweightForPlaceCard)).reduceLeft(getMax)
    //drop active pokemon from hand
    GameManager.setActivePokemon(Some(pokemonWithMaxWeight.pokemonCard), opponentBoard)
    //populate bench with basePokemon
    populateBench()
    Thread.sleep(1000)
    println("ho messo le carte")
    TurnManager.playerReady()
  }

  private def doTurn(): Unit = {

    GameManager.activePokemonStartTurnChecks(opponentBoard, playerBoard)

    //pesco
    GameManager.drawCard(opponentBoard)
    myHand = opponentBoard.hand

    //metto pokemon in panchina
    populateBench()
    GameManager.notifyObservers(Event.updateBoardsEvent())
    Thread.sleep(1500)

    //evolve all pkm
    evolveAll()
    GameManager.notifyObservers(Event.updateBoardsEvent())
    Thread.sleep(1500)

    //calculate if the retreat of the active pokemon is convenient and Do it
    if (!GameManager.isBenchLocationEmpty(0, opponentBoard) && opponentBoard.activePokemon.get.retreatCost.size <= opponentBoard.activePokemon.get.totalEnergiesStored) {
      try {
        calculateIfWithdrawAndDo()
      } catch {
        case _ : InvalidOperationException => println("non posso ritirare")
      }
    }

    //assignEnergy
    val getEnergy = myHand.filter(energy => energy.isInstanceOf[EnergyCard])
    if (getEnergy.nonEmpty)
      calculateAssignEnergy()
    GameManager.notifyObservers(Event.updateBoardsEvent())
    Thread.sleep(1000)
    //attack
    try {
      if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.last.cost)) {
        println("secondo attacco IA")
        GameManager.confirmAttack(opponentBoard, playerBoard, opponentBoard.activePokemon.get.attacks.last)
      } else if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.head.cost)) {
        println("primo attacco IA")
        GameManager.confirmAttack(opponentBoard, playerBoard, opponentBoard.activePokemon.get.attacks.head)
      }
    }
    catch {
      case ex: Exception => println("Exception caught : " + ex)
    }

    GameManager.activePokemonEndTurnChecks(opponentBoard.activePokemon.get)
    TurnManager.switchTurn()
  }

  override def run() {
    try {
      while (!isCancelled) {
        val event: Event = waitForNextEvent()
        event match {
          case event: Event.BuildGameField => {
            opponentBoard = event.asInstanceOf[BuildGameField].opponentBoard
            playerBoard = event.asInstanceOf[BuildGameField].playerBoard
            myHand = opponentBoard.hand
            placeCards(myHand)
          }
          case event: Event.FlipCoin => turn = if(event.isHead) TurnOwner.Player else TurnOwner.Opponent
          case event: Event.NextTurn if event.turnOwner == TurnOwner.Opponent => doTurn()
          case event: Event.PokemonKO => checkForKo()
          case _ : EndGame => isCancelled = true
          case _ =>
        }
      }
      interrupt()
    } catch {
      case _ : Exception =>
    }
  }

  def cancel() : Unit = {
    isCancelled = true
  }

  private def checkForKo(): Unit = {
    if (opponentBoard.activePokemon.get.isKO) {
      if (opponentBoard.pokemonBench.count(card => card.isDefined) > 0) {
        calculateIfWithdrawAndDo()
      }
    }
  }


  private def populateBench(): Unit = {
    val basePokemons: Seq[PokemonCard] = opponentBoard.hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName == "").asInstanceOf[Seq[PokemonCard]]
    basePokemons.zipWithIndex.foreach {
      case (pkm, i) => if (opponentBoard.pokemonBench.count(card => card.nonEmpty) < 5) {
        GameManager.putPokemonToBench(Some(pkm), opponentBoard.pokemonBench.count(card => card.nonEmpty), opponentBoard)
      }
    }
  }

  private def createWeightedCard(pokemon: PokemonCard, calculateweight: PokemonCard => Int) = Seq(PokemonWithWeightImpl(pokemon, calculateweight(pokemon)))

  private def getMax(pkmweight1: PokemonWithWeightImpl, pkmweight2: PokemonWithWeightImpl): PokemonWithWeightImpl = {
    if (pkmweight1.weight > pkmweight2.weight) pkmweight1 else pkmweight2
  }

  private def calculateweightForPlaceCard(pokemon: PokemonCard): Int = {
    var totalweight = 0
    val getPokemonCard = myHand.filter(pkm => pkm.isInstanceOf[PokemonCard])
    val getEnergy = myHand.filter(energy => energy.isInstanceOf[EnergyCard])
    //evolution in Hand
    if (getPokemonCard.exists(pkm => pkm.asInstanceOf[PokemonCard].evolutionName == pokemon.asInstanceOf[PokemonCard].name))
      totalweight += WeightIa.EvolutionInHand
    //15 x energyType
    totalweight += getEnergy.count(card => card.asInstanceOf[EnergyCard].energyType == pokemon.pokemonTypes.head) * WeightIa.HasEnergy

    totalweight
  }

  private def calculateweightForWithdraw(pokemon: PokemonCard): Int = {

    var totalweight = 0
    //pokemonDmg = -10 x Dmg
    totalweight -= (pokemon.initialHp - pokemon.actualHp)
    //weakPokemon = -30
    if (pokemon.weaknesses.nonEmpty) {
      if (pokemon.weaknesses.head.energyType == playerBoard.activePokemon.get.pokemonTypes.head)
        totalweight -= WeightIa.WeakPokemon
    }
    //15 x energyType
    totalweight += pokemon.totalEnergiesStored * WeightIa.HasEnergy
    //is evolution
    if (pokemon.evolutionName != "") totalweight += WeightIa.IsEvolution
    //is active withdrawCost = 20 x Energy To withdraw
    if (pokemon == opponentBoard.activePokemon.get) {
      totalweight += pokemon.retreatCost.size * WeightIa.WithdrawCost
    }
    if (pokemon.isKO) totalweight += WeightIa.KO
    totalweight
  }

  private def evolveAll(): Unit = {
    val evolution: Seq[PokemonCard] = myHand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName != "").asInstanceOf[Seq[PokemonCard]]
    if (evolution.size > 0)
      evolve(evolution)

    @scala.annotation.tailrec
    def evolve(pokemons: Seq[PokemonCard]): Unit = pokemons match {
      case evolution :: t if opponentBoard.activePokemon.get.name == evolution.evolutionName => {
        opponentBoard.activePokemon = GameManager.evolvePokemon(opponentBoard.activePokemon.get, evolution, opponentBoard)
        evolve(t)
      }
      case evolution :: t if !GameManager.isBenchLocationEmpty(0, opponentBoard) => evolveBench(opponentBoard.pokemonBench.filter(pkm => pkm.isDefined), evolution); evolve(t)
      case List() =>
      case _ =>
    }

    @scala.annotation.tailrec
    def evolveBench(bench: Seq[Option[PokemonCard]], evolution: PokemonCard) {
      bench match {
        case List() =>
        case benchPkm :: _ if benchPkm.get.name == evolution.evolutionName => {
          val getbenchedIndex: Int = opponentBoard.pokemonBench.filter(card => card.isDefined).indexWhere(pkm => pkm.get.name == benchPkm.get.name)
          GameManager.putPokemonToBench(GameManager.evolvePokemon(benchPkm.get, evolution, opponentBoard), getbenchedIndex, opponentBoard)
        } //pokemon found in bench
        case _ :: t => evolveBench(t, evolution)
      }
    }
  }

  private def calculateIfWithdrawAndDo(): Unit = {
    val activePokemonweight: Int = calculateweightForWithdraw(opponentBoard.activePokemon.get)
    val benchPokemonsMaxweight = opponentBoard.pokemonBench.filter(c => c.isDefined).flatMap(bench => createWeightedCard(bench.get, calculateweightForWithdraw)).reduceLeft(getMax)
    val benchIndex = opponentBoard.pokemonBench.indexWhere(pkm => pkm.contains(benchPokemonsMaxweight.pokemonCard))

    if (activePokemonweight < benchPokemonsMaxweight.weight) {
      if (!opponentBoard.activePokemon.get.isKO) {
        GameManager.retreatActivePokemon(benchIndex, opponentBoard)
      } else {
        GameManager.destroyActivePokemon(benchIndex, opponentBoard)
      }
    }
  }

  private def calculateAssignEnergy(): Unit = {
    var returnedIndex = -1
    //controllo se il pokemon attivo ha bisogno di un energia, in quel caso , cerco nella mano quell'energia e la assegno
    returnedIndex = assignEnergy(opponentBoard.activePokemon.get)

    if (returnedIndex == -1) {
      //altrimenti la assegno ad un pokemon in panchina
      if (opponentBoard.pokemonBench.count(c => c.isDefined) > 0)
        returnedIndex = iterateSeqToAssign(opponentBoard.pokemonBench.filter(c => c.isDefined))
    }

    def iterateSeqToAssign(seqToIterate: Seq[Option[PokemonCard]]): Int = {
      var index = -1
      seqToIterate.foreach(pkm => {
        if (index < 0)
          index = assignEnergy(pkm.get)
      })
      index
    }
  }

  private def assignEnergy(pokemon: PokemonCard): Int = {
    var energyIndex = -1
    val pokemonType = pokemon.pokemonTypes.head
    var totalEnergyCount = 0
    if (!pokemon.hasEnergies(pokemon.attacks.last.cost)) {
      energyIndex = myHand.indexWhere(card => card.isInstanceOf[EnergyCard] && card.asInstanceOf[EnergyCard].energyType == pokemonType)

      if (energyIndex == -1) {
        val numberOfColorlessNeededForAtk: Int = pokemon.attacks.last.cost.count(card => card.isInstanceOf[EnergyCard] && card.asInstanceOf[EnergyCard].energyType == EnergyType.Colorless)
        println("IA- non ho energie del colore del pokemon , ne assegno una diversa in quanto l'attacco richiede energie colorless")
        pokemon.energiesMap.filter(p => p._1 != pokemonType).foreach(map => totalEnergyCount += map._2)
        if (numberOfColorlessNeededForAtk < totalEnergyCount)
          energyIndex = myHand.indexWhere(card => card.isInstanceOf[EnergyCard])
      }
      if (energyIndex >= 0)
        GameManager.addEnergyToPokemon(pokemon, myHand(energyIndex).asInstanceOf[EnergyCard], opponentBoard)
    }
    energyIndex
  }

  private def waitForNextEvent(): Event = {
    eventQueue.take()
  }

  def notifyEvent(ev: Event): Boolean = {
    eventQueue.offer(ev)
  }

  override def update(event: Event): Unit = {
    notifyEvent(event)
  }
}

sealed trait PokemonWithWeight {
  def pokemonCard: PokemonCard

  def weightValue: Int
}

case class PokemonWithWeightImpl(_pokemonCard: PokemonCard, weight: Int) extends PokemonWithWeight {
  override def pokemonCard: PokemonCard = _pokemonCard

  override def weightValue: Int = weight
}
