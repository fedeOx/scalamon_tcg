package model.core

import common.Observer
import model.game.{CustomDeck, DeckCard, DeckType, SetType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.flatspec.AnyFlatSpec

class DataLoaderTest extends AnyFlatSpec with MockFactory {
/*
  behavior of "The DataLoader"

  it must "load all the decks fo the specified set" in {
    for (set <- SetType.values) {
      val decks: Map[String, Seq[DeckCard]] = DataLoader.loadDecks(set)
      for (deck <- DeckType.values
           if deck.setType == set) {
        assert(decks.contains(deck.name))
      }
    }
  }

  it must "load a not empty list when loading a single deck" in {
    val l = DataLoader.loadSingleDeck(SetType.Base, DeckType.Base1)
    assert(l.nonEmpty)
  }

  it must "load a not empty list when loading a card set" in {
    val l = DataLoader.loadSet(SetType.Base)
    assert(l.nonEmpty)
  }

  it must "work with every indexed set types" in {
    for (set <- SetType.values) {
      val l = DataLoader.loadSet(set)
      assert(l.nonEmpty)
    }
  }

  it must "work with every indexed decks" in {
    for (set <- SetType.values) {
      for (deck <- DeckType.values
           if deck.setType == set) {
        val l = DataLoader.loadSingleDeck(SetType.Base, DeckType.Base1)
        assert(l.nonEmpty)
      }
    }
  }

  it must "save a new custom deck when required" in {
    val cards: Seq[DeckCard] = DeckCard("base-1", "myPokemon", "rare", 1) :: DeckCard("base-2", "myPokemon2", "rare", 1) ::
      DeckCard("base-2", "myPokemon2", "rare", 1) :: Nil
    DataLoader.saveCustomDeck(CustomDeck("myCustomDeck", SetType.Base, cards))
  }
 */
}
