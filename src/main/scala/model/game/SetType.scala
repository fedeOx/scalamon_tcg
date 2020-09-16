package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

object SetType extends Enumeration {
  type SetType = Value
  val Base: Value = Value("base")
}
