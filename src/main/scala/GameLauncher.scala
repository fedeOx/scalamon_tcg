import io.circe.{Decoder, HCursor, Json}

import scala.io.Source
import io.circe.parser._
import model.Cards.{Card, PokemonCard}
import io.circe.optics.JsonPath

object GameLauncher extends App {

  val inputFile = getClass.getResourceAsStream("/base.json")
  val source = Source.fromInputStream(inputFile)
  val lines = try source.getLines() mkString "\n" finally source.close()
  val parseResult: Json = parse(lines).getOrElse(Json.Null)

  val cursor: HCursor = parseResult.hcursor
  var cards: Seq[Card] = List()
  val supertypePath = JsonPath.root.supertype.string

  for (i <- cursor.values.get) {
    val supertype = supertypePath.getOption(i).get
    if (supertype == "Pokémon") {
      cards = cards :+ i.as[PokemonCard].toOption.get
    }
    /*
    if (supertype == "Energy")
      ???
     */
  }
  cards.foreach(println)
}
