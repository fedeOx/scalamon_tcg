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

    case class ShowDeckCards(deckCards: Seq[DeckCard]) extends Event
    case class BuildGameField(gameField: GameField) extends Event
    case class FlipCoin(coinValue: TurnOwner) extends Event
    case class PlaceCards() extends Event
    case class NextTurn(turnOwner: TurnOwner) extends Event
  }

}
