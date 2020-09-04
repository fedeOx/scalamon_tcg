package model

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}


object EnergyType extends Enumeration {
  type EnergyType = Value
  val water: Value = Value("Water")
  val psychic: Value = Value("Psychic")
  val lightning: Value = Value("Lightning")
  val grass: Value = Value("Grass")
  val fire: Value = Value("Fire")
  val fighting: Value = Value("Fighting")
  val colorless: Value = Value("Colorless")

  implicit val decoder: Decoder[EnergyType] = new Decoder[EnergyType] {
    override def apply(c: HCursor): Result[EnergyType] =
      for {
        t <- c.as[String]
      } yield {
        EnergyType.withName(t)
      }
  }
}
