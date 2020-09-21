package model.ia

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

import common.TurnOwner.TurnOwner
import common.{Observer, TurnOwner}
import model.core.GameManager.{collapseToLeft, putPokemonToBench}
import model.core.{GameManager, TurnManager}
import model.event.Events.Event
import model.event.Events.Event.BuildGameField
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Board, EnergyType}

import scala.None

object Ia extends Thread with Observer {

  private val eventQueue: BlockingQueue[Event] = new ArrayBlockingQueue[Event](20)

  TurnManager.addObserver(this)
  GameManager.addObserver(this)
  private var opponentBoard: Board = _
  private var playerBoard: Board = _
  private var myHand: Seq[Card] = _
  private var turn: TurnOwner = _


  private def placeCards(hand: Seq[Card]): Unit = {
    val basePokemons: Seq[PokemonCard] = hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName == "").asInstanceOf[Seq[PokemonCard]]
    val pokemonWithMaxWeight = basePokemons.flatMap(pkm => createWeightedCard(pkm, calculateweightForPlaceCard)).reduceLeft(getMax)
    //drop active pokemon from hand
    opponentBoard.removeCardFromHand(pokemonWithMaxWeight.pokemonCard)
    opponentBoard.activePokemon = Some(pokemonWithMaxWeight.pokemonCard)
    //populate bench with basePokemon
    populateBench()
    Thread.sleep(1000)
    TurnManager.playerReady()
  }

  private def doTurn(): Unit = {
    //TODO QUI
    //pesco
    opponentBoard.addCardsToHand(opponentBoard.popDeck(1))
    myHand = opponentBoard.hand
    //metto pokemon in panchina
    populateBench()
    Thread.sleep(1500)
    GameManager.notifyObservers(Event.updateBoardsEvent())
    val getEnergy = myHand.filter(energy => energy.isInstanceOf[EnergyCard])
    //evolve all pkm
    evolveAll()
    Thread.sleep(1500)
    GameManager.notifyObservers(Event.updateBoardsEvent())
    //calculate if the retreat of the active pokemon is convenient and Do it
    if (opponentBoard.pokemonBench.count(c => c.isDefined) > 0 && opponentBoard.activePokemon.get.retreatCost.size <= opponentBoard.activePokemon.get.totalEnergiesStored)
      calculateIfWithdrawAndDo()
    //assignEnergy
    if (getEnergy.nonEmpty)
      calculateAssignEnergy()
    Thread.sleep(1500)
    GameManager.notifyObservers(Event.updateBoardsEvent())
    //attack
    if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.last.cost)) {
      // GameManager.confirmAttack(opponentBoard.activePokemon.get.attacks.last)
      opponentBoard.activePokemon.get.attacks.last.effect.get.useEffect(opponentBoard, playerBoard)
    } else if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.head.cost)) {
      opponentBoard.activePokemon.get.attacks.head.effect.get.useEffect(opponentBoard, playerBoard)
      // GameManager.confirmAttack(opponentBoard.activePokemon.get.attacks.head)
    }

    if(opponentBoard.activePokemon.get.isKO || playerBoard.activePokemon.get.isKO)
      GameManager.notifyObservers(Event.pokemonKOEvent())
    TurnManager.switchTurn()
  }

  override def run() {
    try {
      while (true) {
        val event: Event = Ia.waitForNextEvent()
        event match {
          case event: Event.PlaceCards => placeCards(opponentBoard.hand)
          case event: Event.BuildGameField => {
            opponentBoard = event.asInstanceOf[BuildGameField].opponentBoard
            playerBoard = event.asInstanceOf[BuildGameField].playerBoard
            myHand = opponentBoard.hand
            //TODO remove
            placeCards(myHand)
          }
          case event: Event.FlipCoin => turn = event.coinValue
          case event: Event.NextTurn if event.turnOwner == TurnOwner.Opponent => doTurn()
          case event: Event.PokemonKO => checkForKo()
          case _ =>
        }
      }
    }
  }

  private def checkForKo(): Unit = {
    if (opponentBoard.activePokemon.get.isKO) {
      val pokemonKO = opponentBoard.activePokemon.get
      if (opponentBoard.pokemonBench.count(card => card.isDefined) > 0) {
        calculateIfWithdrawAndDo()
        opponentBoard.addCardsToDiscardStack(Seq(pokemonKO))
        opponentBoard.putPokemonInBenchPosition(None, opponentBoard.pokemonBench.filter(c => c.nonEmpty).indexWhere(pkm => pkm.get == pokemonKO))

        if (GameManager.pokemonBench(opponentBoard).filter(c => c.nonEmpty).exists(c => c.get.isKO)){
          for ((c, i) <- collapseToLeft(opponentBoard.pokemonBench).zipWithIndex) {
            opponentBoard.putPokemonInBenchPosition(c, i)
          }
        }
        //TODO code Double

      }
      else
        println("PERSO IA")
    } else {
      println("IA- Pesco carta premio")
      opponentBoard.addCardsToHand(opponentBoard.popPrizeCard(1))
      println("IA- Carte Premio Rimaste : "+opponentBoard.prizeCards)
    }
  }

  private def collapseToLeft[A](bench: Seq[Option[A]]): List[Option[A]] = bench match {
    case h :: t if h.isEmpty => collapseToLeft(t) :+ h
    case h :: t if h.nonEmpty => h :: collapseToLeft(t)
    case _ => Nil
  }


  private def populateBench(): Unit = {
    val basePokemons: Seq[PokemonCard] = opponentBoard.hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName == "").asInstanceOf[Seq[PokemonCard]]
    basePokemons.zipWithIndex.foreach {
      case (pkm, i) => if (opponentBoard.pokemonBench.count(card => card.nonEmpty) < 5) {
        opponentBoard.putPokemonInBenchPosition(Some(pkm), opponentBoard.pokemonBench.count(card => card.nonEmpty))
        opponentBoard.removeCardFromHand(pkm)
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

    //TODO SISTEMARE EVOLUZIONI POKEMON , assegnare energie e danni del pokemon evoluto
    @scala.annotation.tailrec
    def evolve(pokemons: Seq[PokemonCard]): Unit = pokemons match {
      case evolution :: t if opponentBoard.activePokemon.get.name == evolution.evolutionName => {
        TransferEvolutionInfo(evolution, opponentBoard.activePokemon.get)
        opponentBoard.activePokemon = Some(evolution.asInstanceOf[PokemonCard])
        evolve(t)
      }
      case evolution :: t if opponentBoard.pokemonBench.count(pkm => pkm.isDefined) > 0 => evolveBench(opponentBoard.pokemonBench.filter(pkm => pkm.isDefined), evolution); evolve(t)
      case List() =>
      case _ =>
    }

    @scala.annotation.tailrec
    def evolveBench(bench: Seq[Option[PokemonCard]], evolution: PokemonCard) {
      bench match {
        case List() =>
        case benchPkm :: _ if benchPkm.get.name == evolution.evolutionName => {
          val getbenchedIndex: Int = opponentBoard.pokemonBench.filter(card => card.isDefined).indexWhere(pkm => pkm.get.name == benchPkm.get.name)
          TransferEvolutionInfo(evolution, benchPkm.get)
          opponentBoard.putPokemonInBenchPosition(Some(evolution), getbenchedIndex)
        } //pokemon found in bench
        case _ :: t => evolveBench(t, evolution)
      }
    }
  }

  private def calculateIfWithdrawAndDo(): Unit = {
    val activePokemonweight: Int = calculateweightForWithdraw(opponentBoard.activePokemon.get)

    val benchPokemonsMaxweight = opponentBoard.pokemonBench.filter(c => c.isDefined).flatMap(bench => createWeightedCard(bench.get, calculateweightForWithdraw)).reduceLeft(getMax)

    if (activePokemonweight < benchPokemonsMaxweight.weight) {
      if (!opponentBoard.activePokemon.get.isKO)
        opponentBoard.activePokemon.get.removeFirstNEnergies(opponentBoard.activePokemon.get.retreatCost.size)

      val currentActivePkmTmp = opponentBoard.activePokemon
      val benchIndex = opponentBoard.pokemonBench.indexWhere(pkm => pkm.contains(benchPokemonsMaxweight.pokemonCard))
      opponentBoard.activePokemon = Some(benchPokemonsMaxweight.pokemonCard)
      opponentBoard.putPokemonInBenchPosition(currentActivePkmTmp, benchIndex)
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
    if (!pokemon.hasEnergies(pokemon.attacks.last.cost)) {
      energyIndex = myHand.indexWhere(card => card.isInstanceOf[EnergyCard] && card.asInstanceOf[EnergyCard].energyType == pokemon.attacks.last.cost.head)
      if (energyIndex >= 0) {
        //TODO Utils.addEnergy?
        pokemon.addEnergy(myHand(energyIndex).asInstanceOf[EnergyCard])
        //remove energy from hand
        opponentBoard.hand.drop(energyIndex)
      }
    }
    energyIndex
  }

  private def TransferEvolutionInfo(evolution: PokemonCard, previousStage: PokemonCard): Unit = {
    evolution.addDamage(previousStage.initialHp - previousStage.actualHp, Seq(EnergyType.Colorless))
    evolution.immune = previousStage.immune
    evolution.energiesMap = previousStage.energiesMap
    evolution.status = previousStage.status
    //TODO pila scarti
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
