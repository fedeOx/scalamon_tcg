package model.effect

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

sealed trait params {
  def name: String
}

sealed trait nDmgParams extends params {
  def basicDmg: String
  def coinFlipNumber: String
  def coinSide: String
  def enemyToAtk: String
}
sealed trait eachDmgParams extends params {
  def dmgToAdd: String
  def pokemonToApply: String
  def signature: String
}
sealed trait eachEnergyParams extends params {
  def plusDmg: String
  def pokemonToApply: String
  def limitBy: String
  def atkPosition: String
}
sealed trait dmgMyselfParams extends params {
  def dmgMyself: String
}
sealed trait statusParams extends params {
  def pokemonToApply: String
  def firstStatusType: String
  def firstEffectCoin: String
  def secondEffectCoin: String
  def secondStatus: String
}
sealed trait setImmunityParams extends params {
  def tailBounded: String
  def headBounded: String
}
sealed trait recoveryParams extends params {
  def recoveryAmount: String
  def pokemonToApply: String
}
sealed trait discardEnergyParams extends params {
  def discardAmount: String
  def energyType: String
  def pokemonToApply: String
}
sealed trait dmgMyselfOrNotParams extends params {
  def tailDmg: String
  def headDmg: String
}

object nDmgParams {
  implicit val decoder: Decoder[nDmgParams] = new Decoder[nDmgParams] {
    override def apply(c: HCursor): Result[nDmgParams] =
      for {
        _dmg <- c.downField("basicDmgToDo").as[Option[String]]
        _coinFlipNumber <- c.downField("coinFlipNumber").as[Option[String]]
        _coinSide <- c.downField("coinSide").as[Option[String]]
        _enemyToAtk <- c.downField("enemyToAtk").as[Option[String]]
      } yield {
        new nDmgParams {
          override def basicDmg: String = _dmg.get
          override def coinFlipNumber: String = _coinFlipNumber.get
          override def coinSide: String = _coinSide.get
          override def enemyToAtk: String = _enemyToAtk.get
          override def name: String = EffectType.doesNdmg.toString
        }
      }
  }

}
object eachDmgParams {
  def apply(name: String, dmgToAdd: String, pokemonToApply: String, signature: String): eachDmgParams = eachDmgParamsImpl(name, dmgToAdd, pokemonToApply, signature)

  implicit val decoder: Decoder[eachDmgParams] = new Decoder[eachDmgParams] {
    override def apply(c: HCursor): Result[eachDmgParams] =
      for {
        _dmg <- c.downField("dmgToAdd").as[Option[String]]
        _pkmToApply <- c.downField("pokemonToApply").as[Option[String]]
        _signature <- c.downField("signature").as[Option[String]]
      } yield {
        eachDmgParamsImpl(EffectType.eachDmg.toString, _dmg.get, _pkmToApply.get, _signature.get)
      }
  }

  private case class eachDmgParamsImpl(name: String, dmgToAdd: String, pokemonToApply: String, signature: String) extends eachDmgParams

}
object eachEnergyParams {
  def apply(name: String, plusDmg: String, pokemonToApply: String, limitBy: String, atkPosition: String): eachEnergyParams = eachEnergyParamsImpl(name, plusDmg, pokemonToApply, limitBy, atkPosition)

  implicit val decoder: Decoder[eachEnergyParams] = new Decoder[eachEnergyParams] {
    override def apply(c: HCursor): Result[eachEnergyParams] =
      for {
        _plusDmg <- c.downField("plusDmg").as[Option[String]]
        _pkmToApply <- c.downField("pokemonToApply").as[Option[String]]
        _limitBy <- c.downField("limitBy").as[Option[String]]
        _atkPosition <- c.downField("atkPosition").as[Option[String]]
      } yield {
        eachEnergyParamsImpl(EffectType.eachEnergy.toString, _plusDmg.get, _pkmToApply.get, _limitBy.get, _atkPosition.get)
      }
  }

  private case class eachEnergyParamsImpl(name: String, plusDmg: String, pokemonToApply: String, limitBy: String, atkPosition: String) extends eachEnergyParams

}
object dmgMyselfParams {
  def apply(name: String, dmgMyself: String): dmgMyselfParams = dmgMyselfParamsImpl(name, dmgMyself)

  implicit val decoder: Decoder[dmgMyselfParams] = new Decoder[dmgMyselfParams] {
    override def apply(c: HCursor): Result[dmgMyselfParams] =
      for {
        _dmgMyself <- c.downField("dmgMyself").as[Option[String]]
      } yield {
        dmgMyselfParamsImpl(EffectType.doesNDmgAndHitMyself.toString, _dmgMyself.get)
      }
  }

  private case class dmgMyselfParamsImpl(name: String, dmgMyself: String) extends dmgMyselfParams

}
object statusParams {
  def apply(name: String, pokemonToApply: String, firstStatusType: String, firstEffectCoin: String, secondEffectCoin: String, secondStatus: String): statusParams = statusParamsImpl(name, pokemonToApply, firstStatusType, firstEffectCoin, secondEffectCoin, secondStatus)

  implicit val decoder: Decoder[statusParams] = new Decoder[statusParams] {
    override def apply(c: HCursor): Result[statusParams] =
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

  private case class statusParamsImpl(name: String, pokemonToApply: String, firstStatusType: String, firstEffectCoin: String, secondEffectCoin: String, secondStatus: String) extends statusParams

}
object setImmunityParams {
  def apply(name: String, tailBounded: String, headBounded: String): setImmunityParams = setImmunityParamsImpl(name, tailBounded, headBounded)

  implicit val decoder: Decoder[setImmunityParams] = new Decoder[setImmunityParams] {
    override def apply(c: HCursor): Result[setImmunityParams] =
      for {
        _tailBounded <- c.downField("tailBounded").as[Option[String]]
        _headBounded <- c.downField("headBounded").as[Option[String]]
      } yield {
        setImmunityParamsImpl(EffectType.doesNDmgAndSetImmunity.toString, _tailBounded.get, _headBounded.get)
      }
  }
  private case class setImmunityParamsImpl(name: String, tailBounded: String, headBounded: String) extends setImmunityParams

}
object recoveryParams {
  def apply(name: String, recoveryAmount: String, pokemonToApply: String): recoveryParams = recoveryParamsImpl(name, recoveryAmount, pokemonToApply)
  implicit val decoder: Decoder[recoveryParams] = new Decoder[recoveryParams] {
    override def apply(c: HCursor): Result[recoveryParams] =
      for {
        _recoveryAmount <- c.downField("recoveryAmount").as[Option[String]]
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]
      } yield {
        recoveryParamsImpl(EffectType.recovery.toString, _recoveryAmount.get, _pokemonToApply.get)
      }
  }
  private case class recoveryParamsImpl( name: String, recoveryAmount: String, pokemonToApply: String) extends recoveryParams

}
object discardEnergyParams {
  def apply(name: String, discardAmount: String, energyType: String, pokemonToApply:String): discardEnergyParams = discardEnergyParamsImpl(name, discardAmount, energyType, pokemonToApply)
  implicit val decoder: Decoder[discardEnergyParams] = new Decoder[discardEnergyParams] {
    override def apply(c: HCursor): Result[discardEnergyParams] =
      for {
        _discardAmount <- c.downField("discardAmount").as[Option[String]]
        _energyType <- c.downField("energyType").as[Option[String]]
        _pokemonToApply <- c.downField("pokemonToApply").as[Option[String]]

      } yield {
        discardEnergyParamsImpl(EffectType.discardEnergy.toString, _discardAmount.get, _energyType.get,_pokemonToApply.get)
      }
  }
  private case class discardEnergyParamsImpl(name: String, discardAmount: String, energyType: String, pokemonToApply:String) extends discardEnergyParams

}
object dmgMyselfOrNotParams {
  def apply(name: String, tailDmg: String, headDmg: String): dmgMyselfOrNotParams = dmgMyselfOrNotParamsImpl(name, tailDmg, headDmg)
  implicit val decoder: Decoder[dmgMyselfOrNotParams] = new Decoder[dmgMyselfOrNotParams] {
    override def apply(c: HCursor): Result[dmgMyselfOrNotParams] =
      for {
        _tailDmg <- c.downField("tailDmg").as[Option[String]]
        _headDmg <- c.downField("headDmg").as[Option[String]]
      } yield {
        dmgMyselfOrNotParamsImpl(EffectType.doesNDmgAndHitMyself_OR_doesNdmg.toString, _tailDmg.get, _headDmg.get)
      }
  }
  private case class dmgMyselfOrNotParamsImpl(name: String, tailDmg: String, headDmg: String) extends dmgMyselfOrNotParams

}

