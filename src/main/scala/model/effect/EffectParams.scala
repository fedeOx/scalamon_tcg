package model.effect

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.game.EnergyType.EnergyType
import model.game.StatusType.StatusType

sealed trait Params {
  def name: String
}

sealed trait NDmgParams extends Params {
  def basicDmg: Int

  def coinFlipNumber: Int

  def coinSide: String

  def enemyToAtk: String
}

sealed trait ToBenchParams extends Params {
  val headDmgTo: String

  val tailDmgTo: String

  val benchToApply: String

  val dmgToDo: Int

  val limit: Int
}

sealed trait EachDmgParams extends Params {
  val dmgToAdd: Int

  val pokemonToApply: String

  val signature: String
}

sealed trait EachEnergyParams extends Params {
  val plusDmg: Int

  val pokemonToApply: String

  val limitBy: Int

  val atkPosition: Int
}

sealed trait DmgMyselfParams extends Params {
  val dmgMyself: Int
}

sealed trait StatusParams extends Params {
  val pokemonToApply: String

  val firstStatusType: StatusType

  val firstEffectCoin: String

  val secondEffectCoin: String

  val secondStatus: StatusType
}

sealed trait SetImmunityParams extends Params {
  val tailBounded: Boolean

  val headBounded: Boolean
}

sealed trait RecoveryParams extends Params {
  val recoveryAmount: Int

  val pokemonToApply: String

  val headBounded: Boolean

  val tailBounded: Boolean
}

sealed trait DiscardEnergyParams extends Params {
  val discardAmount: Int

  val energyType: EnergyType

  val pokemonToApply: String
}

sealed trait DmgMyselfOrNotParams extends Params {
  val tailDmg: Int

  val headDmg: Int
}

sealed trait PreventParams extends Params {
  val dmgToPrevent: Int
}

object NDmgParams {
  implicit val decoder: Decoder[NDmgParams] = new Decoder[NDmgParams] {
    override def apply(c: HCursor): Result[NDmgParams] =
      for {
        _dmg <- c.downField("basicDmgToDo").as[Option[Int]]
        _coinFlipNumber <- c.downField("coinFlipNumber").as[Option[Int]]
        _coinSide <- c.downField("coinSide").as[Option[String]]
        _enemyToAtk <- c.downField("enemyToAtk").as[Option[String]]
      } yield {
        new NDmgParams {
          override def basicDmg: Int = _dmg.get

          override def coinFlipNumber: Int = _coinFlipNumber.get

          override def coinSide: String = _coinSide.get

          override def enemyToAtk: String = _enemyToAtk.get

          override def name: String = EffectType.doesNdmg.toString
        }
      }
  }

}

object ToBenchParams {
  def apply(headDmgTo: String, tailDmgTo: String, benchToApply: String, dmgToDo: Int, limit: Int): ToBenchParams = ToBenchParamsImpl(EffectType.eachDmg.toString, headDmgTo, tailDmgTo, benchToApply, dmgToDo, limit)

  implicit val decodeToBench: Decoder[ToBenchParams] = Decoder.forProduct5("headDmg", "tailDmg", "benchToApply", "dmgToDo", "limit")(ToBenchParams.apply)

  private case class ToBenchParamsImpl(override val name: String, override val headDmgTo: String, override val tailDmgTo: String, override val benchToApply: String, override val dmgToDo: Int, override val limit: Int) extends ToBenchParams

}

object EachDmgParams {
  def apply(dmgToAdd: Int, pokemonToApply: String, signature: String): EachDmgParams = eachDmgParamsImpl(EffectType.eachDmg.toString, dmgToAdd, pokemonToApply, signature)

  implicit val decodeEachDmg: Decoder[EachDmgParams] = Decoder.forProduct3("dmgToAdd", "pokemonToApply", "signature")(EachDmgParams.apply)

  private case class eachDmgParamsImpl(override val name: String, override val dmgToAdd: Int, override val pokemonToApply: String,override val signature: String) extends EachDmgParams

}

object EachEnergyParams {
  def apply(plusDmg: Int, pokemonToApply: String, limitBy: Int, atkPosition: Int): EachEnergyParams = eachEnergyParamsImpl(EffectType.doesNDmgAndHitMyself.toString, plusDmg, pokemonToApply, limitBy, atkPosition)

  implicit val decodeEachEnergy: Decoder[EachEnergyParams] = Decoder.forProduct4("plusDmg", "pokemonToApply", "limitBy", "atkPosition")(EachEnergyParams.apply)

  private case class eachEnergyParamsImpl(override val name: String,override val plusDmg: Int, override val pokemonToApply: String, override val limitBy: Int, override val atkPosition: Int) extends EachEnergyParams

}

object DmgMyselfParams {
  def apply(dmgMyself: Int): DmgMyselfParams = dmgMyselfParamsImpl(EffectType.doesNDmgAndHitMyself.toString, dmgMyself)

  implicit val decodeDmgMyself: Decoder[DmgMyselfParams] = Decoder.forProduct1("dmgMyself")(DmgMyselfParams.apply)

  private case class dmgMyselfParamsImpl(override val name: String, override val dmgMyself: Int) extends DmgMyselfParams

}

object StatusParams {
  def apply(pokemonToApply: String, firstStatusType: StatusType, firstEffectCoin: String, secondEffectCoin: String, secondStatus: StatusType): StatusParams = statusParamsImpl(EffectType.status.toString, pokemonToApply, firstStatusType, firstEffectCoin, secondEffectCoin, secondStatus)

  implicit val decodeDmgMyself: Decoder[StatusParams] = Decoder.forProduct5("pokemonToApply", "firstStatusType", "firstEffectCoin", "secondEffectCoin", "secondStatus")(StatusParams.apply)

  private case class statusParamsImpl(override val name: String, override val pokemonToApply: String,override val firstStatusType: StatusType,override val firstEffectCoin: String,override val secondEffectCoin: String, override val secondStatus: StatusType) extends StatusParams

}


object SetImmunityParams {
  def apply(tailBounded: Boolean, headBounded: Boolean): SetImmunityParams = setImmunityParamsImpl(EffectType.doesNDmgAndSetImmunity.toString, tailBounded, headBounded)

  implicit val decodeSetImmunity: Decoder[SetImmunityParams] = Decoder.forProduct2("tailBounded", "headBounded")(SetImmunityParams.apply)

  private case class setImmunityParamsImpl(override val name: String,override val  tailBounded: Boolean,override val  headBounded: Boolean) extends SetImmunityParams

}

object RecoveryParams {
  def apply(recoveryAmount: Int, pokemonToApply: String, headBounded: Boolean, tailBounded: Boolean): RecoveryParams = recoveryParamsImpl(EffectType.recovery.toString, recoveryAmount, pokemonToApply, headBounded, tailBounded)

  implicit val decodeRecovery: Decoder[RecoveryParams] = Decoder.forProduct4("recoveryAmount", "pokemonToApply", "headBounded", "tailBounded")(RecoveryParams.apply)

  private case class recoveryParamsImpl(override val name: String,override val  recoveryAmount: Int, override val pokemonToApply: String,override val headBounded: Boolean,override val tailBounded: Boolean) extends RecoveryParams

}

object DiscardEnergyParams {
  def apply(discardAmount: Int, energyType: EnergyType, pokemonToApply: String): DiscardEnergyParams = discardEnergyParamsImpl(EffectType.discardEnergy.toString, discardAmount, energyType, pokemonToApply)

  implicit val decodeDiscardEnergy: Decoder[DiscardEnergyParams] = Decoder.forProduct3("discardAmount", "energyType", "pokemonToApply")(DiscardEnergyParams.apply)

  private case class discardEnergyParamsImpl(override val name: String,override val discardAmount: Int,override val energyType: EnergyType,override val pokemonToApply: String) extends DiscardEnergyParams

}

object DmgMyselfOrNotParams {
  def apply(tailDmg: Int, headDmg: Int): DmgMyselfOrNotParams = dmgMyselfOrNotParamsImpl(EffectType.doesNDmgAndHitMyself_OR_doesNdmg.toString, tailDmg, headDmg)

  implicit val decodeDmgMyselfOrNot: Decoder[DmgMyselfOrNotParams] = Decoder.forProduct2("tailDmg", "headDmg")(DmgMyselfOrNotParams.apply)

  private case class dmgMyselfOrNotParamsImpl(override val name: String,override val tailDmg: Int,override val  headDmg: Int) extends DmgMyselfOrNotParams

}

object PreventParams {
  def apply(dmgToPrevent: Int): PreventParams = PreventParamsImpl(EffectType.prevent.toString, dmgToPrevent)
  implicit val decodePreventParams: Decoder[PreventParams] = Decoder.forProduct1("dmgToPrevent")(PreventParams.apply)
  private case class PreventParamsImpl(override val name: String,override val dmgToPrevent: Int) extends PreventParams

}