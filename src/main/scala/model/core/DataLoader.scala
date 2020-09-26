package model.core

import java.io.{File, FileInputStream, InputStream, PrintWriter}
import java.nio.file.{Files, Path, Paths}

import common.Observable
import io.circe.optics.JsonPath
import io.circe.parser.parse
import io.circe.{HCursor, Json}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.DeckType.DeckType
import model.game.{CustomDeck, DeckCard, DeckType}
import model.game.SetType.SetType
import io.circe.syntax._

import scala.collection.mutable
import scala.io.Source

object DataLoader extends Observable {

  private val SaveDirectory: Path = Paths.get(System.getProperty("user.home") + File.separator + ".scalamon")
  private val CustomDeckFileName: String = "/d_custom.json"

  def loadDecks(set: SetType): Map[String, Seq[DeckCard]] = {
    val map: mutable.Map[String, Seq[DeckCard]] = mutable.Map()
    DeckType.values.filter(v => v.setType == set).foreach(v => map += (v.name -> loadSingleDeck(set, v)))
    loadCustomDecksNames(set).map(n => map += (n -> loadCustomSingleDeck(set, n)))
    Map(map.toSeq: _*)
  }

  def loadSingleDeck(set: SetType, deck: DeckType): Seq[DeckCard] =
    buildDeck(buildCursor(getClass.getResourceAsStream("/jsons/d_" + set + ".json")), deck.name)

  def loadCustomSingleDeck(set: SetType, deckName: String): Seq[DeckCard] =
    buildDeck(buildCursor(new FileInputStream(SaveDirectory + CustomDeckFileName)), deckName)

  def loadSet(setType: SetType): Seq[Card] =
    buildSet(buildCursor(getClass.getResourceAsStream("/jsons/" + setType + ".json")))

  def saveCustomDeck(deck: CustomDeck): Unit = {
    var deckCards: Seq[CustomDeck] = List()
    if (Files.exists(Paths.get(SaveDirectory + CustomDeckFileName))) {
      val cursor = buildCursor(new FileInputStream(SaveDirectory + CustomDeckFileName))
      deckCards = cursor.values.get.map(v => v.as[CustomDeck].toOption.get).toList
    }

    if (!Files.exists(SaveDirectory)) {
      Files.createDirectory(SaveDirectory)
    }
    val pw = new PrintWriter(SaveDirectory + CustomDeckFileName)
    pw.write((deckCards :+ deck).asJson.toString)
    pw.close()
  }

  private def loadCustomDecksNames(set: SetType): Seq[String] = {
    val cursor = buildCursor(new FileInputStream(SaveDirectory + CustomDeckFileName))
    if (cursor.values.nonEmpty) {
      cursor.values.get.filter(c => c.hcursor.downField("set").as[SetType].toOption.get == set)
        .map(c => c.hcursor.downField("name").as[String].toOption.get).toList
    } else {
      List.empty
    }
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

  private def buildDeck(cursor: HCursor, deckName: String): Seq[DeckCard] = {
    var deckCards: Seq[DeckCard] = List()
    for (i <- cursor.values.get) {
      val internalCursor = i.hcursor
      if (internalCursor.downField("name").as[String].toOption.get == deckName) {
        deckCards = internalCursor.downField("cards").as[Seq[DeckCard]].toOption.get
      }
    }
    deckCards.filter(c => c.imageId.toInt < 70 || c.imageId.toInt > 95) // excludes Trainer cards
  }

  private def buildSet(cursor: HCursor): Seq[Card] = {
    val supertypePath = JsonPath.root.supertype.string
    var pokemonCards: Seq[Card] = List()
    var energyCards: Seq[Card] = List()
    for (i <- cursor.values.get) {
      val supertype = supertypePath.getOption(i).get
      if (supertype == "Pokémon") {
        pokemonCards = pokemonCards :+ i.as[PokemonCard].toOption.get
      }
      if (supertype == "Energy") {
        energyCards = energyCards :+ i.as[EnergyCard].toOption.get
      }
    }
    pokemonCards ++ energyCards
  }
}