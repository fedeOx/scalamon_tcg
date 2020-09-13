package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

object EnergyType extends Enumeration {
  type EnergyType = Value
  val Water: Value = Value("Water")
  val Psychic: Value = Value("Psychic")
  val Lightning: Value = Value("Lightning")
  val Grass: Value = Value("Grass")
  val Fire: Value = Value("Fire")
  val Fighting: Value = Value("Fighting")
  val Colorless: Value = Value("Colorless")

  implicit val decoder: Decoder[EnergyType] = new Decoder[EnergyType] {
    override def apply(c: HCursor): Result[EnergyType] =
      for {
        t <- c.as[String]
      } yield {
        EnergyType.withName(t)
      }
  }
}
