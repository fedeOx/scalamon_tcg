package model

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.EnergyType.EnergyType

trait Resistance {
  def energyType: EnergyType
  def reduction: Int
}
object Resistance {
  implicit val decoder: Decoder[Resistance] = new Decoder[Resistance] {
    override def apply(c: HCursor): Result[Resistance] =
      for {
        t <- c.downField("type").as[String]
        value <- c.downField("value").as[String]
      } yield {
        new Resistance {
          override def energyType: EnergyType = EnergyType.withName(t)
          override def reduction: Int = value.toInt.abs
        }
      }
  }
}
