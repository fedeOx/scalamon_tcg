package model.core

import java.io.{File, FileInputStream, InputStream, PrintWriter}
import java.nio.file.{Files, Path, Paths}

import common.Observable
import io.circe.optics.JsonPath
import io.circe.parser.parse
import io.circe.{HCursor, Json}
import model.game.DeckType.DeckType
import model.game.{CustomDeck, DeckCard, DeckType}
import model.game.SetType.SetType
import io.circe.syntax._
import model.card.{Card, EnergyCard, PokemonCard}

import scala.collection.mutable
import scala.io.Source

trait DataLoader extends Observable {
  /**
   * Loads all decks of the specified cards set.
   * @param set the set whose decks must be loaded
   * @return a map whose entries contains the deck name as key and the deck's list of cards as value
   */
  def loadDecks(set: SetType): Map[String, Seq[DeckCard]]

  /**
   * Loads all custom decks.
   * @return a map whose entries contains the deck name as key and the deck's list of cards as value
   */
  def loadCustomDecks(): Map[String, Seq[DeckCard]]

  /**
   * Loads the specified deck belonging to the specified set.
   * @param deck the deck to be loaded
   * @return the deck's list of cards
   */
  def loadSingleDeck(deck: DeckType): Seq[DeckCard]

  /**
   * Loads the specified custom deck.
   * @param deckName the name of the deck to be loaded
   * @return the deck's list of cards or an empty list if the deck name specified does not exist
   */
  def loadSingleCustomDeck(deckName: String): Seq[DeckCard]

  /**
   * Loads all the card belonging to the specified set.
   * @param set the set whose cards must be loaded
   * @return the list of card belonging to the specified set
   */
  def loadSet(set: SetType): Seq[Card]

  /**
   * Saves the specified custom deck.
   * @param deck the new custom deck to be saved
   * @return true if the save was successful
   */
  def saveCustomDeck(deck: CustomDeck): Boolean
}

object DataLoader {
  def apply(): DataLoader = DataLoaderImpl()

  val SaveDirectory: Path = Paths.get(System.getProperty("user.home") + File.separator + ".scalamon")
  val CustomDeckFileName: String = "/d_custom.json"
  val CardsDirectory: String = "/jsons"
  val DeckResCode: String = "/d_"
  val ResExtension: String = ".json"

  private case class DataLoaderImpl() extends DataLoader {

    import common.MyMapHelpers._

    override def loadDecks(set: SetType): Map[String, Seq[DeckCard]] = DeckType.values.filter(v => v.setType == set)
      .foldLeft(mutable.Map[String, Seq[DeckCard]]())((m, v) => m += (v.name -> loadSingleDeck(v))).toImmutableMap

    override def loadCustomDecks(): Map[String, Seq[DeckCard]] = loadCustomDecksNames()
      .foldLeft(mutable.Map[String, Seq[DeckCard]]())((m, n) => m += (n -> loadSingleCustomDeck(n))).toImmutableMap

    override def loadSingleDeck(deck: DeckType): Seq[DeckCard] =
      buildDeck(buildCursor(inputStream(CardsDirectory + DeckResCode + deck.setType + ResExtension, fromResource = true)), deck.name)

    override def loadSingleCustomDeck(deckName: String): Seq[DeckCard] =
      buildDeck(buildCursor(inputStream(SaveDirectory + CustomDeckFileName, fromResource = false)), deckName)

    override def loadSet(set: SetType): Seq[Card] =
      buildSet(buildCursor(inputStream(CardsDirectory + "/" + set + ResExtension, fromResource = true)))

    override def saveCustomDeck(deck: CustomDeck): Boolean = {
      var success = false
      var deckCards: Seq[CustomDeck] = List()
      if (!Files.exists(SaveDirectory)) {
        Files.createDirectory(SaveDirectory)
      }
      if (Files.exists(Paths.get(SaveDirectory + CustomDeckFileName))) {
        val cursor = buildCursor(inputStream(SaveDirectory + CustomDeckFileName, fromResource = false))
        deckCards = cursor.values.get.map(v => v.as[CustomDeck].toOption.get).toList
      }
      if (!deckCards.exists(d => d.name == deck.name)) {
        val pw = new PrintWriter(SaveDirectory + CustomDeckFileName)
        pw.write((deckCards :+ deck).asJson.toString)
        pw.close()
        success = true
      }
      success
    }

    private def loadCustomDecksNames(): Seq[String] = {
      var list: Seq[String] = List.empty
      if (Files.exists(Paths.get(SaveDirectory + CustomDeckFileName))) {
        val cursor = buildCursor(inputStream(SaveDirectory + CustomDeckFileName, fromResource = false))
        if (cursor.values.nonEmpty) {
          list = cursor.values.get.map(c => c.hcursor.downField("name").as[String].toOption.get).toList
        }
      }
      list
    }

    private def buildCursor(inputFile: Option[InputStream]): HCursor = {
      def _lines: String = inputFile match {
        case Some(f) => Source.fromInputStream(f, "UTF-8").getLines().foldLeft("")((lines, s) => lines + s + "\n")
        case None => ""
      }
      parse(_lines).getOrElse(Json.Null).hcursor
    }

    private def buildDeck(cursor: HCursor, deckName: String): Seq[DeckCard] = {
      var deckCards: Seq[DeckCard] = List()
      if (cursor.values.nonEmpty) {
        for (i <- cursor.values.get) {
          val internalCursor = i.hcursor
          if (internalCursor.downField("name").as[String].toOption.get == deckName) {
            deckCards = internalCursor.downField("cards").as[Seq[DeckCard]].toOption.get
          }
        }
      }
      deckCards
    }

    private def buildSet(cursor: HCursor): Seq[Card] =
      cursor.values.get.foldLeft(List[Card]())((l, i) => JsonPath.root.supertype.string.getOption(i).get match {
        case "Pokémon" => i.as[PokemonCard].toOption.get :: l
        case "Energy" => i.as[EnergyCard].toOption.get :: l
        case _ => l
      })

    private def inputStream(resourcePath: String, fromResource: Boolean): Option[InputStream] = fromResource match {
      case true => Option(getClass.getResourceAsStream(resourcePath))
      case false if Files.exists(Paths.get(resourcePath)) => Some(new FileInputStream(resourcePath))
      case _ => None
    }
  }
}