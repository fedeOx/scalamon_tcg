package model.effect

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

sealed trait Params {
  def name: String
}

sealed trait NDmgParams extends Params {
  def basicDmg: String
  def coinFlipNumber: String
  def coinSide: String
  def enemyToAtk: String
}
sealed trait EachDmgParams extends Params {
  def dmgToAdd: String
  def pokemonToApply: String
  def signature: String
}
sealed trait EachEnergyParams extends Params {
  def plusDmg: String
  def pokemonToApply: String
  def limitBy: String
  def atkPosition: String
}
sealed trait DmgMyselfParams extends Params {
  def dmgMyself: String
}
sealed trait StatusParams extends Params {
  def pokemonToApply: String
  def firstStatusType: String
  def firstEffectCoin: String
  def secondEffectCoin: String
  def secondStatus: String
}
sealed trait SetImmunityParams extends Params {
  def tailBounded: String
  def headBounded: String
}
sealed trait RecoveryParams extends Params {
  def recoveryAmount: String
  def pokemonToApply: String
}
sealed trait DiscardEnergyParams extends Params {
  def discardAmount: String
  def energyType: String
  def pokemonToApply: String
}
sealed trait DmgMyselfOrNotParams extends Params {
  def tailDmg: String
  def headDmg: String
}

object NDmgParams {
  implicit val decoder: Decoder[NDmgParams] = new Decoder[NDmgParams] {
    override def apply(c: HCursor): Result[NDmgParams] =
      for {
        _dmg <- c.downField("basicDmgToDo").as[Option[String]]
        _coinFlipNumber <- c.downField("coinFlipNumber").as[Option[String]]
        _coinSide <- c.downField("coinSide").as[Option[String]]
        _enemyToAtk <- c.downField("enemyToAtk").as[Option[String]]
      } yield {
        new NDmgParams {
          override def basicDmg: String = _dmg.get
          override def coinFlipNumber: String = _coinFlipNumber.get
          override def coinSide: String = _coinSide.get
          override def enemyToAtk: String = _enemyToAtk.get
          override def name: String = EffectType.doesNdmg.toString
        }
      }
  }

}
object EachDmgParams {
  def apply(name: String, dmgToAdd: String, pokemonToApply: String, signature: String): EachDmgParams = eachDmgParamsImpl(name, dmgToAdd, pokemonToApply, signature)

  implicit val decoder: Decoder[EachDmgParams] = new Decoder[EachDmgParams] {
    override def apply(c: HCursor): Result[EachDmgParams] =
      for {
        _dmg <- c.downField("dmgToAdd").as[Option[String]]
        _pkmToApply <- c.downField("pokemonToApply").as[Option[String]]
        _signature <- c.downField("signature").as[Option[String]]
      } yield {
        eachDmgParamsImpl(EffectType.eachDmg.toString, _dmg.get, _pkmToApply.get, _signature.get)
      }
  }

  private case class eachDmgParamsImpl(name: String, dmgToAdd: String, pokemonToApply: String, signature: String) extends EachDmgParams

}
object EachEnergyParams {
  def apply(name: String, plusDmg: String, pokemonToApply: String, limitBy: String, atkPosition: String): EachEnergyParams = eachEnergyParamsImpl(name, plusDmg, pokemonToApply, limitBy, atkPosition)

  implicit val decoder: Decoder[EachEnergyParams] = new Decoder[EachEnergyParams] {
    override def apply(c: HCursor): Result[EachEnergyParams] =
      for {
        _plusDmg <- c.downField("plusDmg").as[Option[String]]
        _pkmToApply <- c.downField("pokemonToApply").as[Option[String]]
        _limitBy <- c.downField("limitBy").as[Option[String]]
        _atkPosition <- c.downField("atkPosition").as[Option[String]]
      } yield {
        eachEnergyParamsImpl(EffectType.eachEnergy.toString, _plusDmg.get, _pkmToApply.get, _limitBy.get, _atkPosition.get)
      }
  }

  private case class eachEnergyParamsImpl(name: String, plusDmg: String, pokemonToApply: String, limitBy: String, atkPosition: String) extends EachEnergyParams

}
object DmgMyselfParams {
  def apply(name: String, dmgMyself: String): DmgMyselfParams = dmgMyselfParamsImpl(name, dmgMyself)

  implicit val decoder: Decoder[DmgMyselfParams] = new Decoder[DmgMyselfParams] {
    override def apply(c: HCursor): Result[DmgMyselfParams] =
      for {
        _dmgMyself <- c.downField("dmgMyself").as[Option[String]]
      } yield {
        dmgMyselfParamsImpl(EffectType.doesNDmgAndHitMyself.toString, _dmgMyself.get)
      }
  }

  private case class dmgMyselfParamsImpl(name: String, dmgMyself: String) extends DmgMyselfParams

}
object StatusParams {
  def apply(name: String, pokemonToApply: String, firstStatusType: String, firstEffectCoin: String, secondEffectCoin: String, secondStatus: String): StatusParams = statusParamsImpl(name, pokemonToApply, firstStatusType, firstEffectCoin, secondEffectCoin, secondStatus)

  implicit val decoder: Decoder[StatusParams] = new Decoder[StatusParams] {
    override def apply(c: HCursor): Result[StatusParams] =
      for {
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]
        _firstStatusType <- c.downField("firstStatusType").as[Option[String]]
        _firstEffectCoin <- c.downField("firstEffectCoin").as[Option[String]]
        _secondEffectCoin <- c.downField("secondEffectCoin").as[Option[String]]
        _secondStatus <- c.downField("secondStatus").as[Option[String]]
      } yield {
        statusParamsImpl(EffectType.status.toString, _pokemonToApply.get, _firstStatusType.get, _firstEffectCoin.get, _secondEffectCoin.get, _secondStatus.get)
      }
  }

  private case class statusParamsImpl(name: String, pokemonToApply: String, firstStatusType: String, firstEffectCoin: String, secondEffectCoin: String, secondStatus: String) extends StatusParams

}
object SetImmunityParams {
  def apply(name: String, tailBounded: String, headBounded: String): SetImmunityParams = setImmunityParamsImpl(name, tailBounded, headBounded)

  implicit val decoder: Decoder[SetImmunityParams] = new Decoder[SetImmunityParams] {
    override def apply(c: HCursor): Result[SetImmunityParams] =
      for {
        _tailBounded <- c.downField("tailBounded").as[Option[String]]
        _headBounded <- c.downField("headBounded").as[Option[String]]
      } yield {
        setImmunityParamsImpl(EffectType.doesNDmgAndSetImmunity.toString, _tailBounded.get, _headBounded.get)
      }
  }
  private case class setImmunityParamsImpl(name: String, tailBounded: String, headBounded: String) extends SetImmunityParams

}
object RecoveryParams {
  def apply(name: String, recoveryAmount: String, pokemonToApply: String): RecoveryParams = recoveryParamsImpl(name, recoveryAmount, pokemonToApply)
  implicit val decoder: Decoder[RecoveryParams] = new Decoder[RecoveryParams] {
    override def apply(c: HCursor): Result[RecoveryParams] =
      for {
        _recoveryAmount <- c.downField("recoveryAmount").as[Option[String]]
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]
      } yield {
        recoveryParamsImpl(EffectType.recovery.toString, _recoveryAmount.get, _pokemonToApply.get)
      }
  }
  private case class recoveryParamsImpl( name: String, recoveryAmount: String, pokemonToApply: String) extends RecoveryParams

}
object DiscardEnergyParams {
  def apply(name: String, discardAmount: String, energyType: String, pokemonToApply:String): DiscardEnergyParams = discardEnergyParamsImpl(name, discardAmount, energyType, pokemonToApply)
  implicit val decoder: Decoder[DiscardEnergyParams] = new Decoder[DiscardEnergyParams] {
    override def apply(c: HCursor): Result[DiscardEnergyParams] =
      for {
        _discardAmount <- c.downField("discardAmount").as[Option[String]]
        _energyType <- c.downField("energyType").as[Option[String]]
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]

      } yield {
        discardEnergyParamsImpl(EffectType.discardEnergy.toString, _discardAmount.get, _energyType.get,_pokemonToApply.get)
      }
  }
  private case class discardEnergyParamsImpl(name: String, discardAmount: String, energyType: String, pokemonToApply:String) extends DiscardEnergyParams

}
object DmgMyselfOrNotParams {
  def apply(name: String, tailDmg: String, headDmg: String): DmgMyselfOrNotParams = dmgMyselfOrNotParamsImpl(name, tailDmg, headDmg)
  implicit val decoder: Decoder[DmgMyselfOrNotParams] = new Decoder[DmgMyselfOrNotParams] {
    override def apply(c: HCursor): Result[DmgMyselfOrNotParams] =
      for {
        _tailDmg <- c.downField("tailDmg").as[Option[String]]
        _headDmg <- c.downField("headDmg").as[Option[String]]
      } yield {
        dmgMyselfOrNotParamsImpl(EffectType.doesNDmgAndHitMyself_OR_doesNdmg.toString, _tailDmg.get, _headDmg.get)
      }
  }
  private case class dmgMyselfOrNotParamsImpl(name: String, tailDmg: String, headDmg: String) extends DmgMyselfOrNotParams

}

