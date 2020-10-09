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
  def benchToApply : String
  def dmgToDo : Int
  def limit : Int
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
  def headBounded:Boolean
  def tailBounded:Boolean
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
sealed trait PreventParams extends  Params{
  def dmgToPrevent : Int
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
  def apply(name:String ,headDmgTo: String ,tailDmgTo: String , benchToApply : String ,dmgToDo : Int, limit : Int): ToBenchParams = ToBenchParamsImpl(name ,headDmgTo ,tailDmgTo, benchToApply  ,dmgToDo , limit )
  implicit val decoder: Decoder[ToBenchParams] = new Decoder[ToBenchParams] {
    override def apply(c: HCursor): Result[ToBenchParams] =
      for {
        _headDmgTo <- c.downField("headDmg").as[String]
        _tailDmgTo <- c.downField("tailDmg").as[String]
        _benchToApply <- c.downField("benchToApply").as[Option[String]]
        _dmgToDo <- c.downField("dmgToDo").as[Option[Int]]
        _limit <- c.downField("limit").as[Option[Int]]
      } yield {
        ToBenchParamsImpl(EffectType.eachDmg.toString,_headDmgTo,_tailDmgTo, _benchToApply.get, _dmgToDo.get,_limit.get)
      }
  }
  private case class ToBenchParamsImpl(name:String ,headDmgTo: String ,tailDmgTo: String, benchToApply : String ,dmgToDo : Int, limit : Int) extends ToBenchParams

}

object EachDmgParams {
  def apply(name: String, dmgToAdd: Int, pokemonToApply: String, signature: String): EachDmgParams = eachDmgParamsImpl(name, dmgToAdd, pokemonToApply, signature)

  implicit val decoder: Decoder[EachDmgParams] = new Decoder[EachDmgParams] {
    override def apply(c: HCursor): Result[EachDmgParams] =
      for {
        _dmg <- c.downField("dmgToAdd").as[Option[Int]]
        _pkmToApply <- c.downField("pokemonToApply").as[Option[String]]
        _signature <- c.downField("signature").as[Option[String]]
      } yield {
        eachDmgParamsImpl(EffectType.eachDmg.toString, _dmg.get, _pkmToApply.get, _signature.get)
      }
  }
  private case class eachDmgParamsImpl(name: String, dmgToAdd: Int, pokemonToApply: String, signature: String) extends EachDmgParams

}
object EachEnergyParams {
  def apply(name: String, plusDmg: Int, pokemonToApply: String, limitBy: Int, atkPosition: Int): EachEnergyParams = eachEnergyParamsImpl(name, plusDmg, pokemonToApply, limitBy, atkPosition)

  implicit val decoder: Decoder[EachEnergyParams] = new Decoder[EachEnergyParams] {
    override def apply(c: HCursor): Result[EachEnergyParams] =
      for {
        _plusDmg <- c.downField("plusDmg").as[Option[Int]]
        _pkmToApply <- c.downField("pokemonToApply").as[Option[String]]
        _limitBy <- c.downField("limitBy").as[Option[Int]]
        _atkPosition <- c.downField("atkPosition").as[Option[Int]]
      } yield {
        eachEnergyParamsImpl(EffectType.eachEnergy.toString, _plusDmg.get, _pkmToApply.get, _limitBy.get, _atkPosition.get)
      }
  }

  private case class eachEnergyParamsImpl(name: String, plusDmg: Int, pokemonToApply: String, limitBy: Int, atkPosition: Int) extends EachEnergyParams

}
object DmgMyselfParams {
  def apply(name: String, dmgMyself: Int): DmgMyselfParams = dmgMyselfParamsImpl(name, dmgMyself)

  implicit val decoder: Decoder[DmgMyselfParams] = new Decoder[DmgMyselfParams] {
    override def apply(c: HCursor): Result[DmgMyselfParams] =
      for {
        _dmgMyself <- c.downField("dmgMyself").as[Option[Int]]
      } yield {
        dmgMyselfParamsImpl(EffectType.doesNDmgAndHitMyself.toString, _dmgMyself.get)
      }
  }

  private case class dmgMyselfParamsImpl(name: String, dmgMyself: Int) extends DmgMyselfParams

}
object StatusParams {
  def apply(name: String, pokemonToApply: String, firstStatusType: StatusType, firstEffectCoin: String, secondEffectCoin: String, secondStatus: StatusType): StatusParams = statusParamsImpl(name, pokemonToApply, firstStatusType, firstEffectCoin, secondEffectCoin, secondStatus)

  implicit val decoder: Decoder[StatusParams] = new Decoder[StatusParams] {
    override def apply(c: HCursor): Result[StatusParams] =
      for {
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]
        _firstStatusType <- c.downField("firstStatusType").as[Option[StatusType]]
        _firstEffectCoin <- c.downField("firstEffectCoin").as[Option[String]]
        _secondEffectCoin <- c.downField("secondEffectCoin").as[Option[String]]
        _secondStatus <- c.downField("secondStatus").as[Option[StatusType]]
      } yield {
        statusParamsImpl(EffectType.status.toString, _pokemonToApply.get, _firstStatusType.get, _firstEffectCoin.get, _secondEffectCoin.get, _secondStatus.get)
      }
  }

  private case class statusParamsImpl(name: String, pokemonToApply: String, firstStatusType: StatusType, firstEffectCoin: String, secondEffectCoin: String, secondStatus: StatusType) extends StatusParams

}
object SetImmunityParams {
  def apply(name: String, tailBounded: Boolean, headBounded: Boolean): SetImmunityParams = setImmunityParamsImpl(name, tailBounded, headBounded)

  implicit val decoder: Decoder[SetImmunityParams] = new Decoder[SetImmunityParams] {
    override def apply(c: HCursor): Result[SetImmunityParams] =
      for {
        _tailBounded <- c.downField("tailBounded").as[Option[Boolean]]
        _headBounded <- c.downField("headBounded").as[Option[Boolean]]
      } yield {
        setImmunityParamsImpl(EffectType.doesNDmgAndSetImmunity.toString, _tailBounded.get, _headBounded.get)
      }
  }
  private case class setImmunityParamsImpl(name: String, tailBounded: Boolean, headBounded: Boolean) extends SetImmunityParams

}
object RecoveryParams {
  def apply(name: String, recoveryAmount: Int, pokemonToApply: String,headBounded:Boolean,tailBounded:Boolean): RecoveryParams = recoveryParamsImpl(name, recoveryAmount, pokemonToApply,headBounded,tailBounded)
  implicit val decoder: Decoder[RecoveryParams] = new Decoder[RecoveryParams] {
    override def apply(c: HCursor): Result[RecoveryParams] =
      for {
        _recoveryAmount <- c.downField("recoveryAmount").as[Option[Int]]
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]
        _headBounded <- c.downField("headBounded").as[Option[Boolean]]
        _tailBounded <- c.downField("tailBounded").as[Option[Boolean]]
      } yield {
        recoveryParamsImpl(EffectType.recovery.toString, _recoveryAmount.get, _pokemonToApply.get,_headBounded.get,_tailBounded.get)
      }
  }
  private case class recoveryParamsImpl( name: String, recoveryAmount: Int, pokemonToApply: String,headBounded:Boolean,tailBounded:Boolean) extends RecoveryParams

}
object DiscardEnergyParams {
  def apply(name: String, discardAmount: Int, energyType: EnergyType, pokemonToApply:String): DiscardEnergyParams = discardEnergyParamsImpl(name, discardAmount, energyType, pokemonToApply)
  implicit val decoder: Decoder[DiscardEnergyParams] = new Decoder[DiscardEnergyParams] {
    override def apply(c: HCursor): Result[DiscardEnergyParams] =
      for {
        _discardAmount <- c.downField("discardAmount").as[Option[Int]]
        _energyType <- c.downField("energyType").as[Option[EnergyType]]
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]

      } yield {
        discardEnergyParamsImpl(EffectType.discardEnergy.toString, _discardAmount.get, _energyType.get,_pokemonToApply.get)
      }
  }
  private case class discardEnergyParamsImpl(name: String, discardAmount: Int, energyType: EnergyType, pokemonToApply:String) extends DiscardEnergyParams

}
object DmgMyselfOrNotParams {
  def apply(name: String, tailDmg: Int, headDmg: Int): DmgMyselfOrNotParams = dmgMyselfOrNotParamsImpl(name, tailDmg, headDmg)
  implicit val decoder: Decoder[DmgMyselfOrNotParams] = new Decoder[DmgMyselfOrNotParams] {
    override def apply(c: HCursor): Result[DmgMyselfOrNotParams] =
      for {
        _tailDmg <- c.downField("tailDmg").as[Option[Int]]
        _headDmg <- c.downField("headDmg").as[Option[Int]]
      } yield {
        dmgMyselfOrNotParamsImpl(EffectType.doesNDmgAndHitMyself_OR_doesNdmg.toString, _tailDmg.get, _headDmg.get)
      }
  }
  private case class dmgMyselfOrNotParamsImpl(name: String, tailDmg: Int, headDmg: Int) extends DmgMyselfOrNotParams

}

object PreventParams {
  def apply(name: String, dmgToPrevent: Int): PreventParams = PreventParamsImpl(name, dmgToPrevent)
  implicit val decoder: Decoder[PreventParams] = new Decoder[PreventParams] {
    override def apply(c: HCursor): Result[PreventParams] =
      for {
        _dmgToPrevent <- c.downField("dmgToPrevent").as[Option[Int]]
      } yield {
        PreventParamsImpl(EffectType.prevent.toString, _dmgToPrevent.get)
      }
  }
  private case class PreventParamsImpl(name: String, dmgToPrevent: Int) extends PreventParams

}