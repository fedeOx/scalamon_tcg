package model

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.EnergyType.EnergyType
import model.Weakness.Operation.Operation

trait Weakness {
  def energyType: EnergyType
  def operation: Operation
}
object Weakness {
  object Operation extends Enumeration {
    type Operation = Value
    val multiply: Value = Value("x2")
    val subtract: Value = Value("-30")
  }

  implicit val decoder: Decoder[Weakness] = new Decoder[Weakness] {
    override def apply(c: HCursor): Result[Weakness] =
      for {
        t <- c.downField("type").as[String]
        value <- c.downField("value").as[String]
      } yield {
        new Weakness {
          override def energyType: EnergyType = EnergyType.withName(t)
          override def operation: Operation = Operation.withName(value)
        }
      }
  }
}
