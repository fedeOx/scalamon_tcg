package model.core

import common.Observer
import model.game.{DeckType, SetType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.flatspec.AnyFlatSpec

class DataLoaderTest extends AnyFlatSpec with MockFactory {

  behavior of "The DataLoader"

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

  it must "load a not empty list when loading a deck" in {
    //val l = DataLoader.loadDefaultDeck(SetType.Base, DeckType.Base1) TODO
    //assert(l.nonEmpty)
  }

  it must "work with every indexed decks" in {
    for (set <- SetType.values) {
      for (deck <- DeckType.values
           if deck.setType == set) {
        //val l = DataLoader.loadDefaultDeck(set, deck) TODO
        //assert(l.nonEmpty)
      }
    }
  }
}
