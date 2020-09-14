package model.core

import model.exception.CardNotFoundException
import model.game.Cards.{Card, PokemonCard}
import model.game.{DeckCard, DeckType, GameField, SetType}
import org.scalatest.flatspec.AnyFlatSpec

class GameManagerTest extends AnyFlatSpec {

  behavior of "The GameManager"

  val cardsSet: Seq[Card] = DataLoader.loadSet(SetType.Base)
  var playerDeckCards: Seq[DeckCard] = DataLoader.loadDeck(SetType.Base, DeckType.Base1)
  val opponentDeckCards: Seq[DeckCard] = DataLoader.loadDeck(SetType.Base, DeckType.Base2)

  it should "build game field correctly" in {
    val gameField: GameField = GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)

    assert(gameField.playerBoard.deck.nonEmpty && gameField.opponentBoard.deck.nonEmpty)
    assert(gameField.playerBoard.activePokemon.isEmpty && gameField.opponentBoard.activePokemon.isEmpty)
    assert(gameField.playerBoard.hand.size == GameManager.InitialHandCardNum && gameField.opponentBoard.hand.size == GameManager.InitialHandCardNum)
    assert(gameField.playerBoard.prizeCards.size == GameManager.InitialPrizeCardNum && gameField.opponentBoard.prizeCards.size == GameManager.InitialPrizeCardNum)
    assert(gameField.playerBoard.discardStack.isEmpty && gameField.opponentBoard.discardStack.isEmpty)
    assert(gameField.playerBoard.pokemonBench.isEmpty && gameField.opponentBoard.pokemonBench.isEmpty)
  }

  it should "build game field in a way that initially each player must have at least one PokemonCard in their hands" in {
    for (set <- SetType.values) {
      for (deck <- DeckType.values
           if deck.setType == set) {
        val cardsSet: Seq[Card] = DataLoader.loadSet(set)
        val playerDeckCards: Seq[DeckCard] = DataLoader.loadDeck(set, deck)
        val opponentDeckCards: Seq[DeckCard] = DataLoader.loadDeck(set, deck)
        val gameField: GameField = GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
        assert(gameField.playerBoard.hand.exists(c => c.isInstanceOf[PokemonCard]) && gameField.opponentBoard.hand.exists(c => c.isInstanceOf[PokemonCard]))
      }
    }
  }

  it should "throw CardNotFoundException if a DeckCard does not exists in Cards set" in {
    val nonExistentCard: DeckCard = DeckCard("nonExistentID", "sausages", "very rare", 3)
    playerDeckCards = playerDeckCards :+ nonExistentCard

    intercept[CardNotFoundException] {
      GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    }
  }
}
