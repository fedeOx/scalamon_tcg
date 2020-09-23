package model.event

import common.TurnOwner.TurnOwner
import model.game.{Board, DeckCard}

object Events {
  trait Event

  object Event {
    def showDeckCardsEvent(deckCards: Seq[DeckCard]): Event = ShowDeckCards(deckCards)

    def buildGameFieldEvent(playerBoard: Board, opponentBoard: Board): Event = BuildGameField(playerBoard, opponentBoard)

    def flipCoinEvent(coinValue: TurnOwner): Event = FlipCoin(coinValue)

    def placeCardsEvent(): Event = PlaceCards()

    def nextTurnEvent(turnOwner: TurnOwner): Event = NextTurn(turnOwner)

    def updateBoardsEvent(): Event = UpdateBoards()

    def pokemonKOEvent(isAttackingPokemonKO: Boolean = false): Event = PokemonKO(isAttackingPokemonKO)

    def endGameEvent(): Event = EndGame()

    case class ShowDeckCards(deckCards: Seq[DeckCard]) extends Event
    case class BuildGameField(playerBoard: Board, opponentBoard: Board) extends Event
    case class FlipCoin(coinValue: TurnOwner) extends Event
    case class PlaceCards() extends Event
    case class NextTurn(turnOwner: TurnOwner) extends Event
    case class UpdateBoards() extends Event
    case class PokemonKO(isAttackingPokemonKO: Boolean) extends Event
    case class EndGame() extends Event
  }

}
