package model.game

import model.core.{DataLoader, GameManager}
import model.exception.BenchPokemonException
import model.game.Cards.EnergyCard.EnergyCardType
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import org.scalatest.flatspec.AnyFlatSpec

class BoardTest extends AnyFlatSpec {

  val cards: Seq[Card] = DataLoader.loadSet(SetType.Base)
  val board: Board = Board(cards)

  behavior of "A Board"

  it should "pop cards from deck and add them to hand when requested" in {
    val initialHandCardsNumber = board.hand.size
    val initialDeckSize = board.deck.size
    board.addCardsToHand(board.popDeck(1))
    assert(board.hand.size == initialHandCardsNumber + 1)
    assert(board.deck.size == initialDeckSize - 1)
  }

  it should "pop cards from deck and add them to prize cards during game initialization" in {
    val TypicalPrizeCardsNumberToPop = 6
    val initialPrizeCardsNumber = board.prizeCards.size
    val initialDeckSize = board.deck.size
    board.addCardsToPrizeCards(board.popDeck(TypicalPrizeCardsNumberToPop))
    assert(board.prizeCards.size == initialPrizeCardsNumber + TypicalPrizeCardsNumberToPop)
    assert(board.deck.size == initialDeckSize - TypicalPrizeCardsNumberToPop)
  }

  it should "add/remove pokemon to/from bench if it is possible" in {
    val pokemonToAdd: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    for (i <- board.pokemonBench.indices) {
      assert(board.pokemonBench(i).isEmpty)
      board.addPokemonToBench(pokemonToAdd, i)
      assert(board.pokemonBench(i).nonEmpty)
    }

    for (i <- board.pokemonBench.indices) {
      assert(board.pokemonBench(i).nonEmpty)
      board.removePokemonFromBench(i)
      assert(board.pokemonBench(i).isEmpty)
    }

    intercept[BenchPokemonException] {
      board.addPokemonToBench(pokemonToAdd, board.pokemonBench.indices.size)
    }

    intercept[BenchPokemonException] {
      board.removePokemonFromBench(board.pokemonBench.indices.size)
    }
  }

}
