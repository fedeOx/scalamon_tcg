package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.game.EnergyType.EnergyType

trait Resistance {
  def energyType: EnergyType
  def reduction: Int
}
object Resistance {
  implicit val decoder: Decoder[Resistance] = new Decoder[Resistance] {
    override def apply(c: HCursor): Result[Resistance] =
      for {
        _type <- c.downField("type").as[String]
        _value <- c.downField("value").as[String]
      } yield {
        new Resistance {
          override def energyType: EnergyType = EnergyType.withName(_type)
          override def reduction: Int = _value.toInt.abs
        }
      }
  }
}
