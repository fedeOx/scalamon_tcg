package model.game

import model.core.DataWriter
import org.scalatest.flatspec.AnyFlatSpec

class DataWriterTest extends AnyFlatSpec {

  "The DataWriter" should "save a new custom deck when required" in {
    val cards: Seq[DeckCard] = DeckCard("base-1", "myPokemon", "rare", 1) :: DeckCard("base-2", "myPokemon2", "rare", 1) ::
      DeckCard("base-2", "myPokemon2", "rare", 1) :: Nil
    DataWriter.saveCustomDeck(CustomDeck("myCustomDeck", SetType.Base, cards))
  }

}
