package model.pokemonEffect

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.pokemonEffect.EffectType.effectType

object EffectType extends Enumeration {
  type effectType = Value
  val doesNdmg: Value = Value("ndmg")
  val discardEnergy: Value = Value("discard")
  val status:Value = Value("status")
  val eachEnergy: Value = Value("EachEnergy")
  val eachDmg: Value = Value("EachDmg")
  val doesNDmgAndHitMyself : Value = Value("DoesNDmgAndDmgMyself")
  val doesNDmgAndHitMyself_OR_doesNdmg : Value = Value("DoesNDmgAndDmgMyself_OR_doesNdmg")

  implicit val decoder: Decoder[effectType] = new Decoder[effectType] {
    override def apply(c: HCursor): Result[effectType] =
      for {
        t <- c.downField("name").as[Option[String]]
      } yield {
        EffectType.withName(t.get)
      }
  }
}