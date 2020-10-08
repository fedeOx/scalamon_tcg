package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import model.game.SetType.SetType

trait DeckCard {
  def id: String
  def imageNumber: Int
  def belongingSet: Option[SetType]
  def name: String
  def rarity: String
  def count: Int
}

object DeckCard {
  def apply(id: String, imageNumber: Int, belongingSet: Option[SetType], name: String, rarity: String, count: Int): DeckCard =
    DeckCardImpl(id, imageNumber, belongingSet, name, rarity, count)

  implicit val decoder: Decoder[DeckCard] = new Decoder[DeckCard] {
    override def apply(c: HCursor): Result[DeckCard] =
      for {
        _id <- c.downField("id").as[String]
        _belongingSet <- c.getOrElse[Option[SetType]]("set")(None)
        _name <- c.downField("name").as[String]
        _rarity <- c.downField("rarity").as[String]
        _count <- c.downField("count").as[Int]
      } yield {
        val imageNumber = _id.substring(_id.indexOf("-") + 1).toInt
        DeckCard(_id, imageNumber, _belongingSet, _name, _rarity, _count)
      }
  }

  implicit val encoder: Encoder[DeckCard] = new Encoder[DeckCard] {
    override def apply(card: DeckCard): Json = Json.obj(
      ("id", Json.fromString(card.id)),
      ("set", Json.fromString(card.belongingSet.get.toString)),
      ("name", Json.fromString(card.name)),
      ("rarity", Json.fromString(card.rarity)),
      ("count", Json.fromInt(card.count))
    )
  }

  case class DeckCardImpl(override val id: String,
                          override val imageNumber: Int,
                          override val belongingSet: Option[SetType],
                          override val name: String,
                          override val rarity: String,
                          override val count: Int) extends DeckCard
}
