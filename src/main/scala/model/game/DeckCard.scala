package model.game

import io.circe.{Decoder, Encoder}
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
  def apply(id: String, belongingSet: Option[SetType], name: String, rarity: String, count: Int): DeckCard = {
    val imageNumber = id.substring(id.indexOf("-") + 1).toInt
    DeckCardImpl(id, imageNumber, belongingSet, name, rarity, count)
  }

  implicit val decoder: Decoder[DeckCard] =
    Decoder.forProduct5("id", "set", "name", "rarity", "count")(DeckCard.apply)

  implicit val encoder: Encoder[DeckCard] =
    Encoder.forProduct5("id", "set", "name", "rarity", "count")(c =>
      (c.id, c.belongingSet.get.toString, c.name, c.rarity, c.count))

  case class DeckCardImpl(override val id: String,
                          override val imageNumber: Int,
                          override val belongingSet: Option[SetType],
                          override val name: String,
                          override val rarity: String,
                          override val count: Int) extends DeckCard
}
