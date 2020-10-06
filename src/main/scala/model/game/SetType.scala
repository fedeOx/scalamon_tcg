package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

object SetType extends Enumeration {
  type SetType = Value
  val Base: Value = Value("base")
  val Fossil: Value = Value("fossil")

  implicit val decoder: Decoder[SetType] = new Decoder[SetType] {
    override def apply(c: HCursor): Result[SetType] =
      for {
        t <- c.as[String]
      } yield {
        SetType.withName(t)
      }
  }
}
