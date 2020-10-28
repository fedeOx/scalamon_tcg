package common

import common.TurnOwner.TurnOwner
import model.card.Card
import model.game.{Board, DeckCard}

object Events {
  trait Event

  case class ShowDeckCardsEvent(deckCards: Map[String, Seq[DeckCard]]) extends Event

  case class ShowSetCardsEvent(setCards: Seq[Card]) extends Event

  case class BuildGameFieldEvent(playerBoard: Board, opponentBoard: Board) extends Event

  case class FlipCoinEvent(isHead: Boolean) extends Event

  case class NextTurnEvent(turnOwner: TurnOwner) extends Event

  case class PokemonKOEvent(isPokemonInCharge: Boolean, board: Board) extends Event

  case class CustomDeckSavedEvent(success: Boolean) extends Event

  case class DamageBenchEvent(pokemonToDamage: Int, damage: Int) extends Event

  case class UpdateBoardsEvent() extends Event

  case class EndTurnEvent() extends Event

  case class EndGameEvent() extends Event
}
