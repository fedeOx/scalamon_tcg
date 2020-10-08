package model.event

import common.TurnOwner.TurnOwner
import model.game.Cards.Card
import model.game.{Board, DeckCard}

object Events {
  trait Event

  object Event {
    def showDeckCardsEvent(deckCards: Map[String, Seq[DeckCard]]): Event = ShowDeckCards(deckCards)

    def showSetCardsEvent(setCards: Seq[Card]): Event = ShowSetCards(setCards)

    def attackEnded(): Event = AttackEnded()

    def buildGameFieldEvent(playerBoard: Board, opponentBoard: Board): Event = BuildGameField(playerBoard, opponentBoard)

    def flipCoinEvent(isHead: Boolean): Event = FlipCoin(isHead)

    def nextTurnEvent(turnOwner: TurnOwner): Event = NextTurn(turnOwner)

    def updateBoardsEvent(): Event = UpdateBoards()

    def pokemonKOEvent(isPokemonInCharge: Boolean = false, board: Board): Event = PokemonKO(isPokemonInCharge,board)

    def endGameEvent(): Event = EndGame()

    def customDeckSavedEvent(success: Boolean): Event = CustomDeckSaved(success)

    def damageBenchEffect(pokemonToDamage: Int, damage: Int): Event = DamageBenchEffect(pokemonToDamage, damage)

    case class ShowDeckCards(deckCards: Map[String, Seq[DeckCard]]) extends Event
    case class ShowSetCards(setCards: Seq[Card]) extends Event
    case class BuildGameField(playerBoard: Board, opponentBoard: Board) extends Event
    case class FlipCoin(isHead: Boolean) extends Event
    case class NextTurn(turnOwner: TurnOwner) extends Event
    case class UpdateBoards() extends Event
    case class PokemonKO(isPokemonInCharge: Boolean, board: Board) extends Event
    case class AttackEnded() extends Event
    case class EndGame() extends Event
    case class CustomDeckSaved(success: Boolean) extends Event
    case class DamageBenchEffect(pokemonToDamage: Int, damage: Int) extends Event
  }

}
