package model.core

import common.Observable
import io.circe.optics.JsonPath
import io.circe.parser.parse
import io.circe.{HCursor, Json}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{DeckCard, DeckType, SetType}
import model.game.SetType.SetType

import scala.collection.mutable
import scala.io.Source

object DataLoader extends Observable {

  def loadDecks(set: SetType): Map[String, Seq[DeckCard]] = {
    val map: mutable.Map[String, Seq[DeckCard]] = mutable.Map()
    DeckType.values.filter(v => v.setType == set).foreach(v => map += (v.name -> loadSingleDeck(set.toString, v.name)))
    loadCustomDecksNames(set).map(n => map += (n -> loadSingleDeck("custom", n)))
    Map(map.toSeq: _*)
  }

  def loadSingleDeck(setName: String, deckName: String): Seq[DeckCard] = loadData("/json/d_" + setName + ".json")(cursor => {
    var deckCards: Seq[DeckCard] = List()
    for (i <- cursor.values.get) {
      val internalCursor = i.hcursor
      if (internalCursor.downField("name").as[String].toOption.get == deckName) {
        deckCards = internalCursor.downField("cards").as[Seq[DeckCard]].toOption.get
      }
    }
    deckCards.filter(c => c.imageId.toInt < 70 || c.imageId.toInt > 95) // excludes Trainer cards
  })

  /*
  def loadCustomDecks(set: SetType): Map[String, Seq[DeckCard]] = {

    loadData("/json/d_custom.json")(cursor => {
      val customDecksMap: mutable.Map[String, Seq[DeckCard]] = mutable.Map()
      for (i <- cursor.values.get) {
        val internalCursor = i.hcursor
        val deckName = internalCursor.downField("name").as[String].toOption.get
        val deckCards = internalCursor.downField("cards").as[Seq[DeckCard]].toOption.get
          .filter(c => c.imageId.toInt < 70 || c.imageId.toInt > 95) // excludes Trainer cards
        customDecksMap += (deckName -> deckCards)
      }
      Map(customDecksMap.toSeq: _*)
    })
  }
   */
    // read custom decks file ... loadData("/jsons/d_custom.json")(...)

  def loadSet(setType: SetType): Seq[Card] = loadData("/jsons/" + setType + ".json")(cursor => {
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
  })

  private def loadCustomDecksNames(set: SetType, path: String = "/json/d_custom.json"): Seq[String] = loadData(path)(cursor =>
    cursor.values.get.filter(c => c.hcursor.downField("set").as[SetType].toOption.get == set)
      .map(c => c.hcursor.downField("name").as[String].toOption.get).toList
  )

  private def loadData[A](path: String)(f: HCursor => Seq[A]): Seq[A] = {
    val inputFile = getClass.getResourceAsStream(path)
    val source = Source.fromInputStream(inputFile)
    val lines = try source.getLines() mkString "\n" finally source.close()
    val parseResult: Json = parse(lines).getOrElse(Json.Null)
    val cursor: HCursor = parseResult.hcursor
    f(cursor)
  }
}