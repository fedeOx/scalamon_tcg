package controller

import common.{Observer, TurnOwner}
import common.TurnOwner.TurnOwner
import model.core.{DataLoader, GameManager, TurnManager}
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, FlipCoin, PlaceCards, ShowDeckCards}
import model.game.{Board, DeckCard, DeckType, GameField, SetType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

class ControllerTest extends AnyFlatSpec with MockFactory {

  val controller: Controller = Controller()
  val observerMock: Observer = mock[Observer]
  DataLoader.addObserver(observerMock)
  GameManager.addObserver(observerMock)
  TurnManager.addObserver(observerMock)

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

  def checkBoardCorrectness(board: Board): Boolean = {
    board.deck.nonEmpty &&
    board.activePokemon.isEmpty &&
    board.hand.size == GameManager.InitialHandCardNum &&
    board.prizeCards.size == GameManager.InitialPrizeCardNum &&
    board.discardStack.isEmpty &&
    board.pokemonBench.isEmpty
  }

  def waitForControllerThread(): Unit = {
    Thread.sleep(1000)
  }

}
