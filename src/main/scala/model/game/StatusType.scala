package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.game.EnergyType.EnergyType

object StatusType extends Enumeration {
  type StatusType = Value
  val NoStatus : Value = Value("noStatus")
  val Paralyzed: Value = Value("paralyzed")
  val Confused: Value = Value("confused")
  val Asleep: Value = Value("asleep")
  val Poisoned: Value = Value("poisoned")

  def withNameWithDefault(name: String): Value =
    values.find(_.toString.toLowerCase == name.toLowerCase()).getOrElse(NoStatus)

  implicit val decoder: Decoder[StatusType] = new Decoder[StatusType] {
    override def apply(c: HCursor): Result[StatusType] =
      for {
        t <- c.as[String]
      } yield {
        StatusType.withNameWithDefault(t)
      }
  }
}
