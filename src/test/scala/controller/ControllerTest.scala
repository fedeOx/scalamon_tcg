package controller

import common.{CoinUtil, Observer, TurnOwner}
import common.TurnOwner.TurnOwner
import model.core
import model.core.{DataLoader, GameManager, TurnManager}
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, FlipCoin, NextTurn, ShowDeckCards, ShowSetCards, UpdateBoards}
import model.exception.CoinNotLaunchedException
import model.game.Cards.EnergyCard.EnergyCardType
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType
import model.game.{Board, DeckCard, DeckType, EnergyType, SetType, StatusType}
import org.junit.runner.RunWith
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
      e.isInstanceOf[ShowDeckCards]
    }}).repeat(SetType.values.size)
    SetType.values.foreach(s => controller.loadDecks(s))

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowDeckCards]
    }})
    controller.loadCustomDecks()
    waitForControllerThread()
  }

  it must "make DataLoader notify observers when a new card set are loaded" in {
    controller.dataLoader.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowSetCards]
      e.asInstanceOf[ShowSetCards].setCards.isInstanceOf[Seq[Card]]
    }}).repeat(SetType.values.size)
    SetType.values.foreach(s => controller.loadSet(s))
    waitForControllerThread()
  }

  it must "make DataLoader notify observers when a new custom deck is created" in {
    controller.dataLoader.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowSetCards]
      e.asInstanceOf[ShowSetCards].setCards.isInstanceOf[Seq[Card]]
    }}).repeat(SetType.values.size)
    SetType.values.foreach(s => controller.loadSet(s))
    waitForControllerThread()
  }

  it must "make TurnManager launch a CoinNotLaunchedException if a player is ready before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      controller.playerReady()
    }
  }

  it must "make GameManager notify observers when the game field is ready" in {
    controller.gameManager.addObserver(observerMock)
    inAnyOrder {
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[BuildGameField]
        val event: BuildGameField = e.asInstanceOf[BuildGameField]
        event.playerBoard.isInstanceOf[Board]
        event.opponentBoard.isInstanceOf[Board]
        checkBoardCorrectness(event.playerBoard)
        checkBoardCorrectness(event.opponentBoard)
      }})
    }
    initGame()
  }

  it must "make TurnManager notify observers when both human player and AI player are ready to play" in {
    controller.turnManager.asInstanceOf[TurnManager].flipACoin()
    controller.turnManager.addObserver(observerMock)
    inAnyOrder {
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[NextTurn]
        e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
      }})
    }
    controller.playerReady() // AI player is ready
    controller.playerReady() // Human player is ready
  }

  it must "make TurnManager notify observers when a player ends his turn" in {
    controller.turnManager.asInstanceOf[TurnManager].flipACoin()
    controller.turnManager.addObserver(observerMock)
    initGame()
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurn]
      e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
    }})
    controller.endTurn()
  }

  /*
  it should "add and destroy active pokemon when it is possible" in {
    initGame()
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }})
    assert(controller.gameManager.isPlayerActivePokemonEmpty)
    val activePokemonToAdd: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    controller.addActivePokemon(activePokemonToAdd)

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).twice()
    Given("a new active pokemon to add")
    val newActivePokemonToAdd: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myNewActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    When("an active pokemon is already present")
    assert(!GameManager.isPlayerActivePokemonEmpty)
    Then("an ActivePokemonException should be thrown")
    intercept[ActivePokemonException] {
      controller.addActivePokemon(newActivePokemonToAdd)
    }
    When("the actual active pokemon is destroyed")
    controller.destroyActivePokemon()
    assert(GameManager.isPlayerActivePokemonEmpty)
    Then("the new active pokemon can be added")
    controller.addActivePokemon(newActivePokemonToAdd)
  }

  it should "add and remove pokemon from bench when it is possible" in {
    val BenchSize = 5
    for (i <- 0 until BenchSize) assert(GameManager.isPlayerBenchLocationEmpty(i))

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).repeat(BenchSize)
    val benchPokemonToAdd: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    for (i <- 0 until BenchSize) controller.addPokemonToBench(benchPokemonToAdd, i)
    for (i <- 0 until BenchSize) assert(!GameManager.isPlayerBenchLocationEmpty(i))

    Given("a new bench pokemon to add")
    val newBenchPokemonToAdd: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myNewBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    When("a pokemon is already present in the specified position of the bench")
    assert(!GameManager.isPlayerBenchLocationEmpty(0))
    Then("a BenchPokemonException should be thrown")
    intercept[BenchPokemonException] {
      controller.addPokemonToBench(newBenchPokemonToAdd, 0)
    }

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).repeat(BenchSize)
    for (i <- 0 until BenchSize) controller.destroyPokemonFromBench(i)
    for (i <- 0 until BenchSize) assert(GameManager.isPlayerBenchLocationEmpty(i))
  }
*/

  it must "make GameManager notify observers when a card is draw from deck" in {
    initGame()
    controller.gameManager.addObserver(observerMock)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
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

  private def checkBoardCorrectness(board: Board): Boolean = {
    board.deck.nonEmpty &&
    board.activePokemon.isEmpty &&
    board.hand.size == GameManager.InitialHandCardNum &&
    board.prizeCards.size == GameManager.InitialPrizeCardNum &&
    board.discardStack.isEmpty &&
    !board.pokemonBench.exists(c => c.nonEmpty)
  }

  def waitForControllerThread(): Unit = {
    Thread.sleep(1000)
  }

}
