package controller

import common.Observer
import common.TurnOwner.TurnOwner
import model.card.{Card, EnergyCard, PokemonCard}
import model.core.{DataLoader, GameManager, TurnManager}
import common.Events.{Event, NextTurnEvent, ShowDeckCardsEvent, ShowSetCardsEvent, UpdateBoardsEvent}
import model.exception.CoinNotLaunchedException
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType
import model.game.{DeckCard, EnergyType, SetType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{GivenWhenThen, OneInstancePerTest}
import org.scalatest.flatspec.AnyFlatSpec

class ControllerTest extends AnyFlatSpec with MockFactory with GivenWhenThen with OneInstancePerTest {

  val observerMock: Observer = mock[Observer]
  val controller: Controller = Controller()

  behavior of "A Controller"

  it must "make DataLoader notify observers when new decks are loaded" in {
    controller.dataLoader.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowDeckCardsEvent]
    }}).repeat(SetType.values.size)
    SetType.values.foreach(s => controller.loadDecks(s))

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowDeckCardsEvent]
    }})
    controller.loadCustomDecks()
    waitForControllerThread()
  }

  it must "make DataLoader notify observers when a new card set are loaded" in {
    controller.dataLoader.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowSetCardsEvent]
      e.asInstanceOf[ShowSetCardsEvent].setCards.isInstanceOf[Seq[Card]]
    }}).repeat(SetType.values.size)
    SetType.values.foreach(s => controller.loadSet(s))
    waitForControllerThread()
  }

  it must "make DataLoader notify observers when a new custom deck is created" in {
    controller.dataLoader.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowSetCardsEvent]
      e.asInstanceOf[ShowSetCardsEvent].setCards.isInstanceOf[Seq[Card]]
    }}).repeat(SetType.values.size)
    SetType.values.foreach(s => controller.loadSet(s))
    waitForControllerThread()
  }

  it must "make TurnManager launch a CoinNotLaunchedException if a player is ready before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      controller.playerReady()
    }
  }

  it must "make TurnManager notify observers when both human player and AI player are ready to play" in {
    controller.turnManager.asInstanceOf[TurnManager].flipACoin()
    controller.turnManager.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurnEvent]
      e.asInstanceOf[NextTurnEvent].turnOwner.isInstanceOf[TurnOwner]
    }})
    controller.playerReady() // AI player is ready
    controller.playerReady() // Human player is ready
  }

  it must "make TurnManager notify observers when a player ends his turn" in {
    controller.turnManager.asInstanceOf[TurnManager].flipACoin()
    controller.turnManager.addObserver(observerMock)
    initGame()
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurnEvent]
      e.asInstanceOf[NextTurnEvent].turnOwner.isInstanceOf[TurnOwner]
    }})
    controller.endTurn()
  }

  it should "swap or retreat pokemon when required" in {
    initGame();
    val gameManager = controller.gameManager.asInstanceOf[GameManager]
    val dataLoader = controller.dataLoader.asInstanceOf[DataLoader]

    Given("an active pokemon and a benched pokemon")
    val pokemon: Option[PokemonCard] = getPokemon(dataLoader, SetType.Base, "Bulbasaur")
    val benchedPokemon: Option[PokemonCard] = getPokemon(dataLoader, SetType.Base, "Staryu")
    val benchPosition = 0
    gameManager.putPokemonToBench(benchedPokemon, benchPosition)
    gameManager.setActivePokemon(pokemon)
    val energy: Option[EnergyCard] = getEnergy(dataLoader, SetType.Base, EnergyType.Colorless)
    pokemon.get.addEnergy(energy.get)
    benchedPokemon.get.addEnergy(energy.get)

    When("the operation of swap is triggered")
    controller.swap(benchPosition)

    Then("the active pokemon should be swapped with the benched one")
    assert(gameManager.activePokemon() == benchedPokemon)
    assert(gameManager.pokemonBench()(benchPosition) == pokemon)

    When("the operation of swap is triggered and the active pokemon is KO")
    benchedPokemon.get.addDamage(benchedPokemon.get.initialHp, Seq(EnergyType.Colorless))
    assert(benchedPokemon.get.isKO)
    controller.swap(benchPosition)

    Then("the active pokemon should be swapped with the benched one and destroyed correctly")
    assert(gameManager.activePokemon() == pokemon)
    assert(gameManager.pokemonBench()(benchPosition).isEmpty)
    assert(gameManager.playerBoard.discardStack.contains(benchedPokemon.get))
  }

  it must "make GameManager notify observers when a card is draw from deck" in {
    initGame()
    controller.gameManager.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoardsEvent]
    }})
    controller.drawCard()
  }

  it should "manage the user game routines with active pokemon" in {
    initGame();
    val gameManager = controller.gameManager.asInstanceOf[GameManager]
    val dataLoader =  controller.dataLoader.asInstanceOf[DataLoader]
    val playerBoard = gameManager.playerBoard
    playerBoard.activePokemon = None
    controller.playerReady()

    Given("a base pokemon card selected by the user from his hand")
    val basePokemonCard: Option[PokemonCard] = getPokemon(dataLoader, SetType.Base, "Bulbasaur")
    controller.handCardSelected = basePokemonCard

    When("the active pokemon location is empty and selected by the user")
    assert(playerBoard.activePokemon.isEmpty)
    controller.selectActivePokemonLocation()

    Then("the base pokemon card selected by the user should be placed in active pokemon position")
    assert(playerBoard.activePokemon.nonEmpty)
    assert(playerBoard.activePokemon == basePokemonCard)

    Given("an energy card selected by the user from his hand")
    val energyCard: Option[EnergyCard] = getEnergy(dataLoader, SetType.Base, EnergyType.Grass)
    controller.handCardSelected = energyCard

    When("the active pokemon location is not empty and selected by the user")
    assert(playerBoard.activePokemon.nonEmpty)
    controller.selectActivePokemonLocation()

    Then("the energy card selected by the user should be assigned to the active pokemon")
    assert(playerBoard.activePokemon.nonEmpty)
    assert(playerBoard.activePokemon.get.hasEnergies(Seq(energyCard.get.energyType)))

    Given("a non base pokemon card selected by the user from his hand")
    val nonBasePokemonCard: Option[PokemonCard] = getPokemon(dataLoader, SetType.Base, "Ivysaur")
    controller.handCardSelected = nonBasePokemonCard

    When("the active pokemon location is selected by the user and contains the base pokemon for the one selected from the user hand")
    assert(playerBoard.activePokemon.nonEmpty)
    controller.selectActivePokemonLocation()

    Then("the active pokemon is evolved whit the one selected by the user hand")
    assert(playerBoard.activePokemon.nonEmpty)
    assert(playerBoard.activePokemon == nonBasePokemonCard)
    assert(playerBoard.activePokemon.get.energiesMap == nonBasePokemonCard.get.energiesMap)
    assert(playerBoard.activePokemon.get.status == nonBasePokemonCard.get.status)
    assert(playerBoard.activePokemon.get.actualHp == playerBoard.activePokemon.get.initialHp - (nonBasePokemonCard.get.initialHp - nonBasePokemonCard.get.actualHp))
    assert(playerBoard.discardStack.contains(basePokemonCard.get))
  }

  it should "manage the user game routines with bench pokemons" in {
    initGame();
    val gameManager = controller.gameManager.asInstanceOf[GameManager]
    val dataLoader =  controller.dataLoader.asInstanceOf[DataLoader]
    val playerBoard = gameManager.playerBoard
    playerBoard.activePokemon = None
    controller.playerReady()

    for (i <- playerBoard.pokemonBench.indices) {
      playerBoard.putPokemonInBenchPosition(None, i)
    }
    playerBoard.pokemonBench.foreach(p => assert(p.isEmpty))

    Given("a base pokemon card selected by the user from his hand")
    val basePokemonCard: Option[PokemonCard] = getPokemon(dataLoader, SetType.Base, "Bulbasaur")
    controller.handCardSelected = basePokemonCard

    When("a bench position is empty and selected by the user")
    val position = 0
    assert(playerBoard.pokemonBench.head.isEmpty)
    controller.selectBenchLocation(position)

    Then("the base pokemon card selected by the user should be placed in that position of the bench")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    assert(playerBoard.pokemonBench.head == basePokemonCard)

    Given("an energy card selected by the user from his hand")
    val energyCard: Option[EnergyCard] = getEnergy(dataLoader, SetType.Base, EnergyType.Grass)
    controller.handCardSelected = energyCard

    When("a bench position is not empty and selected by the user")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    controller.selectBenchLocation(position)

    Then("the energy card selected by the user should be assigned to bench pokemon in that position")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    assert(playerBoard.pokemonBench.head.get.hasEnergies(Seq(energyCard.get.energyType)))

    Given("a non base pokemon card selected by the user from his hand")
    val nonBasePokemonCard: Option[PokemonCard] = getPokemon(dataLoader, SetType.Base, "Ivysaur")
    controller.handCardSelected = nonBasePokemonCard

    When("a bench position is selected by the user and contains the base pokemon for the one selected from the user hand")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    controller.selectBenchLocation(position)

    Then("the bench pokemon is evolved whit the one selected by the user hand")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    assert(playerBoard.pokemonBench.head == nonBasePokemonCard)
    assert(playerBoard.pokemonBench.head.get.energiesMap == nonBasePokemonCard.get.energiesMap)
    assert(playerBoard.pokemonBench.head.get.status == nonBasePokemonCard.get.status)
    assert(playerBoard.pokemonBench.head.get.actualHp == playerBoard.pokemonBench.head.get.initialHp - (nonBasePokemonCard.get.initialHp - nonBasePokemonCard.get.actualHp))
    assert(playerBoard.discardStack.contains(basePokemonCard.get))
  }

  private def getPokemon(dataLoader: DataLoader, set: SetType, pokemonName: String): Option[PokemonCard] =
    dataLoader.loadSet(set).filter(p => p.isInstanceOf[PokemonCard] && p.asInstanceOf[PokemonCard].name == pokemonName)
      .map(p => p.asInstanceOf[PokemonCard]).headOption

  private def getEnergy(dataLoader: DataLoader, set: SetType, energyType: EnergyType): Option[EnergyCard] =
    dataLoader.loadSet(set).filter(p => p.isInstanceOf[EnergyCard] && p.asInstanceOf[EnergyCard].energyType == energyType)
      .map(p => p.asInstanceOf[EnergyCard]).headOption

  private def initGame(): Unit = {
    val deckCards: Seq[DeckCard] = loadFirstDeck(controller.dataLoader.asInstanceOf[DataLoader], SetType.Base)
    controller.initGame(deckCards, Seq(SetType.Base))
    waitForControllerThread()
  }

  private def loadFirstDeck(dataLoader: DataLoader, set: SetType): Seq[DeckCard] = {
    val map: Map[String, Seq[DeckCard]] = dataLoader.loadDecks(set)
    var deck: Seq[DeckCard] = List()
    if (map.nonEmpty) {
      deck = map.values.head
    }
    deck
  }

  def waitForControllerThread(): Unit = {
    Thread.sleep(1000)
  }

}
