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
  def headDmgTo: String

  def tailDmgTo: String

  def benchToApply: String

  def dmgToDo: Int

  def limit: Int
}

sealed trait EachDmgParams extends Params {
  def dmgToAdd: Int

  def pokemonToApply: String

  def signature: String
}

sealed trait EachEnergyParams extends Params {
  def plusDmg: Int

  def pokemonToApply: String

  def limitBy: Int

  def atkPosition: Int
}

sealed trait DmgMyselfParams extends Params {
  def dmgMyself: Int
}

sealed trait StatusParams extends Params {
  def pokemonToApply: String

  def firstStatusType: StatusType

  def firstEffectCoin: String

  def secondEffectCoin: String

  def secondStatus: StatusType
}

sealed trait SetImmunityParams extends Params {
  def tailBounded: Boolean

  def headBounded: Boolean
}

sealed trait RecoveryParams extends Params {
  def recoveryAmount: Int

  def pokemonToApply: String

  def headBounded: Boolean

  def tailBounded: Boolean
}

sealed trait DiscardEnergyParams extends Params {
  def discardAmount: Int

  def energyType: EnergyType

  def pokemonToApply: String
}

sealed trait DmgMyselfOrNotParams extends Params {
  def tailDmg: Int

  def headDmg: Int
}

sealed trait PreventParams extends Params {
  def dmgToPrevent: Int
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

  private case class ToBenchParamsImpl(name: String, headDmgTo: String, tailDmgTo: String, benchToApply: String, dmgToDo: Int, limit: Int) extends ToBenchParams

}

object EachDmgParams {
  def apply(dmgToAdd: Int, pokemonToApply: String, signature: String): EachDmgParams = eachDmgParamsImpl(EffectType.eachDmg.toString, dmgToAdd, pokemonToApply, signature)

  implicit val decodeEachDmg: Decoder[EachDmgParams] = Decoder.forProduct3("dmgToAdd", "pokemonToApply", "signature")(EachDmgParams.apply)

  private case class eachDmgParamsImpl(name: String, dmgToAdd: Int, pokemonToApply: String, signature: String) extends EachDmgParams

}

object EachEnergyParams {
  def apply(plusDmg: Int, pokemonToApply: String, limitBy: Int, atkPosition: Int): EachEnergyParams = eachEnergyParamsImpl(EffectType.doesNDmgAndHitMyself.toString, plusDmg, pokemonToApply, limitBy, atkPosition)

  implicit val decodeEachEnergy: Decoder[EachEnergyParams] = Decoder.forProduct4("plusDmg", "pokemonToApply", "limitBy", "atkPosition")(EachEnergyParams.apply)

  private case class eachEnergyParamsImpl(name: String, plusDmg: Int, pokemonToApply: String, limitBy: Int, atkPosition: Int) extends EachEnergyParams

}

object DmgMyselfParams {
  def apply(dmgMyself: Int): DmgMyselfParams = dmgMyselfParamsImpl(EffectType.doesNDmgAndHitMyself.toString, dmgMyself)

  implicit val decodeDmgMyself: Decoder[DmgMyselfParams] = Decoder.forProduct1("dmgMyself")(DmgMyselfParams.apply)

  private case class dmgMyselfParamsImpl(name: String, dmgMyself: Int) extends DmgMyselfParams

}

object StatusParams {
  def apply(pokemonToApply: String, firstStatusType: StatusType, firstEffectCoin: String, secondEffectCoin: String, secondStatus: StatusType): StatusParams = statusParamsImpl(EffectType.status.toString, pokemonToApply, firstStatusType, firstEffectCoin, secondEffectCoin, secondStatus)

  implicit val decodeDmgMyself: Decoder[StatusParams] = Decoder.forProduct5("pokemonToApply", "firstStatusType", "firstEffectCoin", "secondEffectCoin", "secondStatus")(StatusParams.apply)

  private case class statusParamsImpl(name: String, pokemonToApply: String, firstStatusType: StatusType, firstEffectCoin: String, secondEffectCoin: String, secondStatus: StatusType) extends StatusParams

}


object SetImmunityParams {
  def apply(tailBounded: Boolean, headBounded: Boolean): SetImmunityParams = setImmunityParamsImpl(EffectType.doesNDmgAndSetImmunity.toString, tailBounded, headBounded)

  implicit val decodeSetImmunity: Decoder[SetImmunityParams] = Decoder.forProduct2("tailBounded", "headBounded")(SetImmunityParams.apply)

  private case class setImmunityParamsImpl(name: String, tailBounded: Boolean, headBounded: Boolean) extends SetImmunityParams

}

object RecoveryParams {
  def apply(recoveryAmount: Int, pokemonToApply: String, headBounded: Boolean, tailBounded: Boolean): RecoveryParams = recoveryParamsImpl(EffectType.recovery.toString, recoveryAmount, pokemonToApply, headBounded, tailBounded)

  implicit val decodeRecovery: Decoder[RecoveryParams] = Decoder.forProduct4("recoveryAmount", "pokemonToApply", "headBounded", "tailBounded")(RecoveryParams.apply)

  private case class recoveryParamsImpl(name: String, recoveryAmount: Int, pokemonToApply: String, headBounded: Boolean, tailBounded: Boolean) extends RecoveryParams

}

object DiscardEnergyParams {
  def apply(discardAmount: Int, energyType: EnergyType, pokemonToApply: String): DiscardEnergyParams = discardEnergyParamsImpl(EffectType.discardEnergy.toString, discardAmount, energyType, pokemonToApply)

  implicit val decodeDiscardEnergy: Decoder[DiscardEnergyParams] = Decoder.forProduct3("discardAmount", "energyType", "pokemonToApply")(DiscardEnergyParams.apply)

  private case class discardEnergyParamsImpl(name: String, discardAmount: Int, energyType: EnergyType, pokemonToApply: String) extends DiscardEnergyParams

}

object DmgMyselfOrNotParams {
  def apply(tailDmg: Int, headDmg: Int): DmgMyselfOrNotParams = dmgMyselfOrNotParamsImpl(EffectType.doesNDmgAndHitMyself_OR_doesNdmg.toString, tailDmg, headDmg)

  implicit val decodeDmgMyselfOrNot: Decoder[DmgMyselfOrNotParams] = Decoder.forProduct2("tailDmg", "headDmg")(DmgMyselfOrNotParams.apply)

  private case class dmgMyselfOrNotParamsImpl(name: String, tailDmg: Int, headDmg: Int) extends DmgMyselfOrNotParams

}

object PreventParams {
  def apply(dmgToPrevent: Int): PreventParams = PreventParamsImpl(EffectType.prevent.toString, dmgToPrevent)
  implicit val decodePreventParams: Decoder[PreventParams] = Decoder.forProduct1("dmgToPrevent")(PreventParams.apply)
  private case class PreventParamsImpl(name: String, dmgToPrevent: Int) extends PreventParams

}