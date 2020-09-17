package model.core

import common.Observable
import io.circe.optics.JsonPath
import io.circe.parser.parse
import io.circe.{HCursor, Json}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.DeckType.DeckType
import model.game.DeckCard
import model.game.SetType.SetType

import scala.io.Source

object DataLoader extends Observable {

  def loadDeck(set: SetType, deck: DeckType): Seq[DeckCard] = {
    loadData("/jsons/d_" + set + ".json")(cursor => {
      var deckCards: Seq[DeckCard] = List()
      for (i <- cursor.values.get) {
        val internalCursor = i.hcursor
        if (internalCursor.downField("name").as[DeckType].toOption.get == deck) {
          deckCards = internalCursor.downField("cards").as[Seq[DeckCard]].toOption.get
        }
      }
      deckCards.filter(c => c.imageId.toInt < 70 || c.imageId.toInt > 95) // excludes Trainer cards
    })
  }

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


  private def loadData[A](path: String)(f: HCursor => Seq[A]): Seq[A] = {
    val inputFile = getClass.getResourceAsStream(path)
    val source = Source.fromInputStream(inputFile)
    val lines = try source.getLines() mkString "\n" finally source.close()
    val parseResult: Json = parse(lines).getOrElse(Json.Null)
    val cursor: HCursor = parseResult.hcursor
    f(cursor)
  }
}