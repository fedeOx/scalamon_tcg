package model.core

import common.Observer
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, ShowDeckCards, UpdateBoards}
import model.exception.CardNotFoundException
import model.game.Cards.{Card, PokemonCard}
import model.game.{Board, DeckCard, DeckType, EnergyType, SetType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.flatspec.AnyFlatSpec

class GameManagerTest extends AnyFlatSpec with MockFactory  {

  behavior of "The GameField"

  val cardsSet: Seq[Card] = DataLoader.loadSet(SetType.Base)
  var playerDeckCards: Seq[DeckCard] = DataLoader.loadDeck(SetType.Base, DeckType.Base1)
  val opponentDeckCards: Seq[DeckCard] = DataLoader.loadDeck(SetType.Base, DeckType.Base2)
  val observerMock: Observer = mock[Observer]
  GameManager.addObserver(observerMock)

  it should "build game field correctly and notify observers when it happens" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[BuildGameField]
      e.asInstanceOf[BuildGameField].playerBoard.isInstanceOf[Board]
      e.asInstanceOf[BuildGameField].opponentBoard.isInstanceOf[Board]
    }})
    GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    val playerBoard = GameManager.playerBoard
    val opponentBoard = GameManager.opponentBoard
    assert(playerBoard.deck.nonEmpty && opponentBoard.deck.nonEmpty)
    assert(playerBoard.activePokemon.isEmpty && opponentBoard.activePokemon.isEmpty)
    assert(playerBoard.hand.size == GameManager.InitialHandCardNum && opponentBoard.hand.size == GameManager.InitialHandCardNum)
    assert(playerBoard.prizeCards.size == GameManager.InitialPrizeCardNum && opponentBoard.prizeCards.size == GameManager.InitialPrizeCardNum)
    assert(playerBoard.discardStack.isEmpty && opponentBoard.discardStack.isEmpty)
    playerBoard.pokemonBench.foreach(c => assert(c.isEmpty))
    opponentBoard.pokemonBench.foreach(c => assert(c.isEmpty))
  }

  it should "build game field in a way that initially each player must have at least one base PokemonCard in their hands" in {
    for (set <- SetType.values) {
      for (deck <- DeckType.values
           if deck.setType == set) {
        val cardsSet: Seq[Card] = DataLoader.loadSet(set)
        val playerDeckCards: Seq[DeckCard] = DataLoader.loadDeck(set, deck)
        val opponentDeckCards: Seq[DeckCard] = DataLoader.loadDeck(set, deck)
        (observerMock.update _).expects(where {e: Event => {
          e.isInstanceOf[BuildGameField]
          e.asInstanceOf[BuildGameField].playerBoard.isInstanceOf[Board]
          e.asInstanceOf[BuildGameField].opponentBoard.isInstanceOf[Board]
        }})
        GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
        assert(GameManager.playerBoard.hand.exists(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].evolutionName.isEmpty)
          && GameManager.opponentBoard.hand.exists(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].evolutionName.isEmpty))
      }
    }
  }

  it should "notify observers when a card is draw from player deck or prize cards stack" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }}).twice()
    GameManager.drawCard(GameManager.playerBoard)
    GameManager.drawPrizeCard(GameManager.playerBoard)
  }

  it should "notify observers when player active pokemon or player bench is updated" in {
    val newActivePokemon: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    val newBenchPokemon: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }}).repeat(3)
    GameManager.setActivePokemon(Some(newActivePokemon))
    GameManager.putPokemonToBench(Some(newBenchPokemon), 0)
    GameManager.destroyActivePokemon(0)
  }

  it should "throw CardNotFoundException if a DeckCard does not exists in Cards set" in {
    val nonExistentCard: DeckCard = DeckCard("nonExistentID", "sausages", "very rare", 3)
    playerDeckCards = playerDeckCards :+ nonExistentCard

    intercept[CardNotFoundException] {
      GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    }
  }
}
