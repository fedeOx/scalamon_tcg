package model.ia

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import common.TurnOwner.TurnOwner
import common.{Observer, TurnOwner}
import model.core.{GameManager, TurnManager}
import model.event.Events.Event
import model.event.Events.Event.BuildGameField
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Board, EnergyType}

object Ia extends Thread with Observer {

  private val eventQueue: BlockingQueue[Event] = new ArrayBlockingQueue[Event](20)

  TurnManager.addObserver(this)
  GameManager.addObserver(this)
  private var opponentBoard: Board = _
  private var playerBoard: Board = _
  private var myHand: Seq[Card] = _
  private var turn: TurnOwner = _
  private val getPokemonCard = myHand.filter(pkm => pkm.isInstanceOf[PokemonCard])
  private val getEnergy = myHand.filter(energy => energy.isInstanceOf[EnergyCard])


  private def placeCards(hand: Seq[Card]): Unit = {
    var basePokemons: Seq[PokemonCard] = hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName == "").asInstanceOf[Seq[PokemonCard]]
    val pokemonWithMaxWeight = basePokemons.flatMap(pkm => createWeightedCard(pkm, calculateweightForPlaceCard)).reduceLeft(getMax)
    //drop active pokemon from hand
    basePokemons = basePokemons.filter(basepkm => basepkm.imageId != pokemonWithMaxWeight.pokemonCard.imageId)
    opponentBoard.activePokemon = Some(pokemonWithMaxWeight.pokemonCard)
    //populate bench with basePokemon
    basePokemons.zipWithIndex.foreach { case (pkm, i) => if (i < 5) opponentBoard.addPokemonToBench(pkm, i) }

    TurnManager.playerReady()
  }

  private def doTurn(): Unit = {
    //evolve all pkm
    evolveAll()
    //calculate if the retreat of the active pokemon is convenient and Do it
    calculateIfWithdrawAndDo()
    //assignEnergy
    if (getEnergy.nonEmpty)
      calculateAssignEnergy()
    //attack
    if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.last.cost)) {
      opponentBoard.activePokemon.get.attacks.last.effect.get.useEffect()
    } else if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.head.cost)) {
      opponentBoard.activePokemon.get.attacks.head.effect.get.useEffect()
    }

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
          case _ =>
        }
      }
    }
  }

  private def createWeightedCard(pokemon: PokemonCard, calculateweight: PokemonCard => Int) = Seq(PokemonWithWeightImpl(pokemon, calculateweight(pokemon)))

  private def getMax(pkmweight1: PokemonWithWeightImpl, pkmweight2: PokemonWithWeightImpl): PokemonWithWeightImpl = {
    if (pkmweight1.weight > pkmweight2.weight) pkmweight1 else pkmweight2
  }

  private def calculateweightForPlaceCard(pokemon: PokemonCard): Int = {
    var totalweight = 0
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
    if (pokemon.weaknesses.head.energyType == playerBoard.activePokemon.get.pokemonTypes)
      totalweight -= WeightIa.WeakPokemon
    //15 x energyType
    totalweight += pokemon.totalEnergiesStored * WeightIa.HasEnergy
    //is evolution
    if (pokemon.evolutionName != "") totalweight += WeightIa.IsEvolution
    //is active withdrawCost = 20 x Energy To withdraw
    if (pokemon == opponentBoard.activePokemon.get) {
      totalweight += pokemon.retreatCost.size * WeightIa.WithdrawCost
    }
    totalweight
  }

  private def evolveAll(): Unit = {
    val evolution: Seq[PokemonCard] = myHand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName != "").asInstanceOf[Seq[PokemonCard]]
    evolve(evolution)

    //TODO SISTEMARE EVOLUZIONI POKEMON , assegnare energie e danni del pokemon evoluto
    @scala.annotation.tailrec
    def evolve(pokemons: Seq[PokemonCard]): Unit = pokemons match {
      case evolution :: t if opponentBoard.activePokemon.get.name == evolution.evolutionName => {
        TransferEvolutionInfo(evolution, opponentBoard.activePokemon.get)
        opponentBoard.activePokemon = Some(evolution.asInstanceOf[PokemonCard])
        evolve(t)
      }
      case evolution :: _ => evolveBench(opponentBoard.pokemonBench, evolution)
      case _ :: t if t.nonEmpty => evolve(t)
      case _ :: _ =>
    }

    @scala.annotation.tailrec
    def evolveBench(bench: Seq[Option[PokemonCard]], evolution: PokemonCard) {
      bench match {
        case benchPkm :: _ if benchPkm.isEmpty => //emptyBench
        case benchPkm :: _ if benchPkm.get.name == evolution.evolutionName => {
          val getbenchedIndex: Int = opponentBoard.pokemonBench.indexWhere(pkm => pkm.get.name == benchPkm.get.name)
          TransferEvolutionInfo(evolution, benchPkm.get)
          opponentBoard.addPokemonToBench(evolution, getbenchedIndex)
        } //pokemon found in bench
        case _ :: t => evolveBench(t, evolution)
      }
    }
  }

  private def calculateIfWithdrawAndDo(): Unit = {
    val activePokemonweight: Int = calculateweightForWithdraw(opponentBoard.activePokemon.get)
    val benchPokemonsMaxweight = opponentBoard.pokemonBench.flatMap(bench => createWeightedCard(bench.get, calculateweightForWithdraw)).reduceLeft(getMax)

    if (activePokemonweight < benchPokemonsMaxweight.weight) {
      println("Cambio il pokemon attivo")
      val currentActivePkmTmp = opponentBoard.activePokemon
      val benchIndex = opponentBoard.pokemonBench.indexWhere(pkm => pkm.contains(benchPokemonsMaxweight.pokemonCard))
      opponentBoard.activePokemon = Some(benchPokemonsMaxweight.pokemonCard)
      opponentBoard.addPokemonToBench(currentActivePkmTmp.get, benchIndex)
    }
  }

  private def calculateAssignEnergy(): Unit = {
    var returnedIndex = -1
    //controllo se il pokemon attivo ha bisogno di un energia, in quel caso , cerco nella mano quell'energia e la assegno
    returnedIndex = assignEnergy(opponentBoard.activePokemon.get)

    //se non serve al pokemon attivo o non ho trovato l'energia giusta in mano, la assegno ad un pokemon evoluto
    val evolutedPkm: Seq[Option[PokemonCard]] = myHand.filter(pkm => pkm.asInstanceOf[PokemonCard].evolutionName != "").asInstanceOf[Seq[Option[PokemonCard]]]
    returnedIndex = iterateSeqToAssign(returnedIndex, evolutedPkm)

    //altrimenti la assegno ad un pokemon in panchina
    returnedIndex = iterateSeqToAssign(returnedIndex, opponentBoard.pokemonBench)

    def iterateSeqToAssign(returnedIndex: Int, seqToIterate: Seq[Option[PokemonCard]]): Int = {
      var index = -1
      if (returnedIndex < 0 && seqToIterate.nonEmpty) {
        seqToIterate.foreach(pkm => {
          if (index < 0)
            index = assignEnergy(pkm.get)
        })
      }
      index
    }
  }

  private def assignEnergy(pokemon: PokemonCard): Int = {
    var energyIndex = -1
    if (!pokemon.hasEnergies(pokemon.attacks.last.cost)) {
      energyIndex = myHand.indexWhere(card => card.isInstanceOf[EnergyCard] && card.asInstanceOf[EnergyCard].energyType == pokemon.attacks.last.cost.head)
      if (energyIndex >= 0) {
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
    //TODO aggiungere energie

    evolution.status = previousStage.status

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