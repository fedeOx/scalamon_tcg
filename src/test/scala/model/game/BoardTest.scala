package model.game

import model.core.{DataLoader, GameManager}
import model.exception.BenchPokemonException
import model.game.Cards.EnergyCard.EnergyCardType
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import org.scalatest.{GivenWhenThen, OneInstancePerTest}
import org.scalatest.flatspec.AnyFlatSpec

class BoardTest extends AnyFlatSpec with GivenWhenThen {

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
    var initialPrizeCardsNumber = board.prizeCards.size
    val initialDeckSize = board.deck.size
    board.addCardsToPrizeCards(board.popDeck(TypicalPrizeCardsNumberToPop))
    assert(board.prizeCards.size == initialPrizeCardsNumber + TypicalPrizeCardsNumberToPop)
    assert(board.deck.size == initialDeckSize - TypicalPrizeCardsNumberToPop)

    val initialHandCardsNumber = board.hand.size
    initialPrizeCardsNumber = board.prizeCards.size
    Given("the prize cards list of a player")
    assert(board.prizeCards.nonEmpty)
    When("that player makes KO an opponent active pokemon and pop a prize card")
    board.addCardsToHand(board.popPrizeCard(1))
    Then("the prize card should be added to the player hand")
    assert(board.prizeCards.size == initialPrizeCardsNumber - 1)
    assert(board.hand.size == initialHandCardsNumber + 1)
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

  it should "add an active pokemon to the discard stack when it is made KO" in {
    val activePokemon: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    board.activePokemon = Some(activePokemon)
    board.addCardsToDiscardStack(board.activePokemon.get :: Nil)
    board.activePokemon = None
    assert(board.discardStack.nonEmpty)
    assert(board.activePokemon.isEmpty)
  }

}
