package model.game

import io.circe.{Decoder, Encoder}

trait CustomDeck {
  def name: String
  def cards: Seq[DeckCard]
}

object CustomDeck {
  def apply(name: String, cards: Seq[DeckCard]): CustomDeck = CustomDeckImpl(name, cards)

  implicit val decoder: Decoder[CustomDeck] = Decoder.forProduct2("name", "cards")(CustomDeck.apply)

  implicit val encoder: Encoder[CustomDeck] = Encoder.forProduct2("name", "cards")(d =>
    (d.name, d.cards)
  )

  case class CustomDeckImpl(override val name: String,
                            override val cards: Seq[DeckCard]) extends CustomDeck
}
