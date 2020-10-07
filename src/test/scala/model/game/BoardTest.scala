package model.game

import model.core.DataLoader
import model.exception.InvalidOperationException
import model.game.Cards.{Card, PokemonCard}
import model.game.SetType.SetType
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec

class BoardTest extends AnyFlatSpec with GivenWhenThen {

  val dataLoader: DataLoader = DataLoader()
  val cards: Seq[Card] = dataLoader.loadSet(SetType.Base)
  val board: Board = Board(cards)

  behavior of "A Board"

  it should "pop cards from deck and add them to the hand when requested" in {
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
    val pokemonToAdd: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")
    for (i <- board.pokemonBench.indices) {
      assert(board.pokemonBench(i).isEmpty)
      board.putPokemonInBenchPosition(pokemonToAdd, i)
      assert(board.pokemonBench(i).nonEmpty)
    }

    for (i <- board.pokemonBench.indices) {
      assert(board.pokemonBench(i).nonEmpty)
      board.putPokemonInBenchPosition(None, i)
      assert(board.pokemonBench(i).isEmpty)
    }

    val outOfBoundPosition = board.pokemonBench.indices.size
    intercept[InvalidOperationException] {
      board.putPokemonInBenchPosition(pokemonToAdd, outOfBoundPosition)
    }

    intercept[InvalidOperationException] {
      board.putPokemonInBenchPosition(None, outOfBoundPosition)
    }
  }

  it should "add an active pokemon to the discard stack when it is made KO" in {
    val activePokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")
    board.activePokemon = activePokemon
    board.addCardsToDiscardStack(board.activePokemon.get :: Nil)
    board.activePokemon = None
    assert(board.discardStack.nonEmpty)
    assert(board.activePokemon.isEmpty)
  }

  private def getPokemon(set: SetType, pokemonName: String): Option[PokemonCard] =
    dataLoader.loadSet(set).filter(p => p.isInstanceOf[PokemonCard] && p.asInstanceOf[PokemonCard].name == pokemonName)
      .map(p => p.asInstanceOf[PokemonCard]).headOption

}
