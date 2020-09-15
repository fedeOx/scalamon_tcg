package model.event

import common.TurnOwner.TurnOwner
import model.game.{DeckCard, GameField}

object Events {
  trait Event

  object Event {
    def showDeckCardsEvent(deckCards: Seq[DeckCard]): Event = ShowDeckCards(deckCards)

    def buildGameFieldEvent(gameField: GameField): Event = BuildGameField(gameField)

    def flipCoinEvent(coinValue: TurnOwner): Event = FlipCoin(coinValue)

    def placeCardsEvent(): Event = PlaceCards()

    def nextTurnEvent(turnOwner: TurnOwner): Event = NextTurn(turnOwner)

    def updatePlayerBoardEvent(): Event = UpdatePlayerBoard()

    def updateOpponentBoardEvent(): Event = UpdateOpponentBoard()

    def updateBothBoardsEvent(): Event = UpdateBothBoards()

    case class ShowDeckCards(deckCards: Seq[DeckCard]) extends Event
    case class BuildGameField(gameField: GameField) extends Event
    case class FlipCoin(coinValue: TurnOwner) extends Event
    case class PlaceCards() extends Event
    case class NextTurn(turnOwner: TurnOwner) extends Event
    case class UpdatePlayerBoard() extends Event
    case class UpdateOpponentBoard() extends Event
    case class UpdateBothBoards() extends Event
  }

}
