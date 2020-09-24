package model.game

import io.circe.Json.JArray
import io.circe.{Encoder, Json}
import model.game.SetType.SetType
import io.circe.syntax._

trait CustomDeck {
  def name: String
  def set: SetType
  def cards: Seq[DeckCard]
}

object CustomDeck {
  def apply(name: String, set: SetType, cards: Seq[DeckCard]): CustomDeck = CustomDeckImpl(name, set, cards)

  implicit val encoder: Encoder[CustomDeck] = new Encoder[CustomDeck] {
    override def apply(deck: CustomDeck): Json = Json.obj(
      ("name", Json.fromString(deck.name)),
      ("set", Json.fromString(deck.set.toString)),
      ("cards", deck.cards.asJson)
    )
  }

  case class CustomDeckImpl(override val name: String,
                            override val set: SetType,
                            override val cards: Seq[DeckCard]) extends CustomDeck
}
