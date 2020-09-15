package controller

import common.{Observer, TurnOwner}
import common.TurnOwner.TurnOwner
import model.core.{DataLoader, GameManager, TurnManager}
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, FlipCoin, NextTurn, PlaceCards, ShowDeckCards, UpdatePlayerBoard}
import model.exception.{ActivePokemonException, BenchPokemonException}
import model.game.Cards.PokemonCard
import model.game.{Board, DeckCard, DeckType, EnergyType, GameField, SetType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec

class ControllerTest extends AnyFlatSpec with MockFactory with GivenWhenThen {
  val controller: Controller = Controller()
  val observerMock: Observer = mock[Observer]
  DataLoader.addObserver(observerMock)
  GameManager.addObserver(observerMock)
  TurnManager.addObserver(observerMock)
  TurnManager.flipACoin()

  behavior of "A Controller"

  it must "make DataLoader notify observers when a new deck is loaded" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowDeckCards]
      e.asInstanceOf[ShowDeckCards].deckCards.isInstanceOf[Seq[DeckCard]]
      e.asInstanceOf[ShowDeckCards].deckCards.nonEmpty
    }})
    controller.loadDeckCards(SetType.Base, DeckType.Base1)
    waitForControllerThread()
  }

  it must "make GameManager and TurnManager notify observers when the GameField is ready and the starting turn coin is launched" in {
    inAnyOrder {
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[BuildGameField]
        val event: BuildGameField = e.asInstanceOf[BuildGameField]
        event.gameField.isInstanceOf[GameField]
        checkBoardCorrectness(event.gameField.playerBoard)
        checkBoardCorrectness(event.gameField.opponentBoard)
      }})
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[FlipCoin]
        val event: FlipCoin = e.asInstanceOf[FlipCoin]
        event.coinValue.isInstanceOf[TurnOwner]
        TurnOwner.values.contains(event.coinValue)
      }})
    }
    val deckCards: Seq[DeckCard] = DataLoader.loadDeck(SetType.Base, DeckType.Base1)
    controller.initGame(deckCards, SetType.Base)
    waitForControllerThread()
  }

  it must "make TurnManager notify observers when the user confirm to start the game" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[PlaceCards]
    }})
    controller.startGame()
    waitForControllerThread()
  }

  it must "make TurnManager notify observers when both human player and AI player are ready to play" in {
    inAnyOrder {
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[NextTurn]
        e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
      }})
    }

    TurnManager.playerReady() // AI player is ready
    controller.playerReady()
  }

  it should "notify observers when a player ends his turn" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurn]
      e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
    }})

    controller.endTurn()
  }

  it should "add and destroy active pokemon when it is possible" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }})
    assert(GameManager.isPlayerActivePokemonEmpty)
    val activePokemonToAdd: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    controller.addActivePokemon(activePokemonToAdd)

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).twice()
    Given("A new active pokemon to add")
    val newActivePokemonToAdd: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myNewActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    When("An active pokemon is already present")
    assert(!GameManager.isPlayerActivePokemonEmpty)
    Then("An ActivePokemonException should be thrown")
    intercept[ActivePokemonException] {
      controller.addActivePokemon(newActivePokemonToAdd)
    }
    When("The actual active pokemon is destroyed")
    controller.destroyActivePokemon()
    assert(GameManager.isPlayerActivePokemonEmpty)
    Then("The new active pokemon can be added")
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

    Given("A new bench pokemon to add")
    val newBenchPokemonToAdd: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myNewBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    When("A pokemon is already present in the specified position of the bench")
    assert(!GameManager.isPlayerBenchLocationEmpty(0))
    Then("A BenchPokemonException should be thrown")
    intercept[BenchPokemonException] {
      controller.addPokemonToBench(newBenchPokemonToAdd, 0)
    }

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).repeat(BenchSize)
    for (i <- 0 until BenchSize) controller.removePokemonFromBench(i)
    for (i <- 0 until BenchSize) assert(GameManager.isPlayerBenchLocationEmpty(i))
  }

  def checkBoardCorrectness(board: Board): Boolean = {
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
