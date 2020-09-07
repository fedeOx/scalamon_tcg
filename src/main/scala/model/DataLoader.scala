package model

import io.circe.optics.JsonPath
import io.circe.{HCursor, Json}
import io.circe.parser.parse
import model.Cards.{Card, EnergyCard, PokemonCard}
import model.SetType.SetType

import scala.io.Source

object DataLoader {

  def loadData(setType: SetType): Seq[Card] = {
    val inputFile = getClass.getResourceAsStream("/" + setType + ".json")
    val source = Source.fromInputStream(inputFile)
    val lines = try source.getLines() mkString "\n" finally source.close()
    val parseResult: Json = parse(lines).getOrElse(Json.Null)
    val cursor: HCursor = parseResult.hcursor
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
