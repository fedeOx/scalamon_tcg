package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

trait DeckCard {
  def imageId: String
  def name: String
  def rarity: String
  def count: Int
}

object DeckCard {
  def apply(id: String, name: String, rarity: String, count: Int): DeckCard = DeckCardImpl(id, name, rarity, count)

  implicit val decoder: Decoder[DeckCard] = new Decoder[DeckCard] {
    override def apply(c: HCursor): Result[DeckCard] =
      for {
        _id <- c.downField("id").as[String]
        _name <- c.downField("name").as[String]
        _rarity <- c.downField("rarity").as[String]
        _count <- c.downField("count").as[Int]
      } yield {
        val imageId = _id.substring(_id.indexOf("-") + 1)
        DeckCard(imageId, _name, _rarity, _count)
      }
  }

  case class DeckCardImpl(override val imageId: String,
                          override val name: String,
                          override val rarity: String,
                          override val count: Int) extends DeckCard
}