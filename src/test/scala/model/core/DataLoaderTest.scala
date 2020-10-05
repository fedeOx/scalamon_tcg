package model.core

import java.io.{FileInputStream, InputStream, PrintWriter}

import io.circe.{HCursor, Json}
import io.circe.parser.parse
import io.circe.syntax._
import model.game.{CustomDeck, DeckCard, DeckType, SetType}
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec

import scala.io.Source

class DataLoaderTest extends AnyFlatSpec with GivenWhenThen {

  behavior of "The DataLoader"

  val dataLoader: DataLoader = DataLoader()

  it must "load all the decks for the specified set" in {
    for (set <- SetType.values) {
      val decks: Map[String, Seq[DeckCard]] = dataLoader.loadDecks(set)
      for (deck <- DeckType.values
           if deck.setType == set) {
        assert(decks.contains(deck.name))
      }
    }
  }

  it must "load a not empty list when loading a single deck" in {
    val l = dataLoader.loadSingleDeck(DeckType.Base1)
    assert(l.nonEmpty)
  }

  it must "load a not empty list when loading a card set" in {
    val l = dataLoader.loadSet(SetType.Base)
    assert(l.nonEmpty)
  }

  it must "load cards for every indexed set types" in {
    for (set <- SetType.values) {
      val l = dataLoader.loadSet(set)
      assert(l.nonEmpty)
    }
  }

  it must "load cards with every indexed decks" in {
    for (set <- SetType.values) {
      for (deck <- DeckType.values
           if deck.setType == set) {
        val l = dataLoader.loadSingleDeck(DeckType.Base1)
        assert(l.nonEmpty)
      }
    }
  }

  it must "save and load custom decks correctly" in {
    Given("a custom deck")
    val deckName: String = "myCustomDeckName"
    val customDeck: Seq[DeckCard] = DeckCard("1", "myPokemon", "rare", 1) :: DeckCard("2", "myPokemon2", "rare", 1) ::
      DeckCard("3", "myPokemon2", "rare", 1) :: Nil

    When("the custom deck is saved")
    dataLoader.saveCustomDeck(CustomDeck(deckName, SetType.Base, customDeck))

    Then("it should be able to be found in the destination file")
    val cursor = buildCursor(new FileInputStream(DataLoader.SaveDirectory + DataLoader.CustomDeckFileName))
    assert(cursor.values.get.map(i => i.hcursor.downField("name")).exists(i => i.as[String].toOption.get == deckName))

    When("an existent custom deck is choosen")
    val existentCustomDeck: Seq[DeckCard] = dataLoader.loadCustomSingleDeck(SetType.Base, deckName)

    Then("the card list of the specified custom deck must be loaded")
    assert(existentCustomDeck.nonEmpty && existentCustomDeck == customDeck)

    resetCustomDecksFile(deckName)
  }

  private def buildCursor(inputFile: InputStream): HCursor = {
    val source = Source.fromInputStream(inputFile)
    var lines = ""
    if (inputFile != null) {
      lines = try source.getLines() mkString "\n" finally source.close()
    }
    val parseResult: Json = parse(lines).getOrElse(Json.Null)
    parseResult.hcursor
  }

  private def resetCustomDecksFile(deckName: String): Unit = {
    val cursor = buildCursor(new FileInputStream(DataLoader.SaveDirectory + DataLoader.CustomDeckFileName))
    val deckCards = cursor.values.get.map(v => v.as[CustomDeck].toOption.get).toList
    val pw = new PrintWriter(DataLoader.SaveDirectory + DataLoader.CustomDeckFileName)
    pw.write((deckCards.filter(d => d.name != deckName)).asJson.toString)
    pw.close()
  }
}
