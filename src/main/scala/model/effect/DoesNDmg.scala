package model.effect

import common.CoinUtil.CoinValue
import model.game.Cards.PokemonCard
import model.game.{Board, EnergyType, StatusType}
import model.game.StatusType.StatusType
import model.effect.utils.{atkTo, getAtkOrDef}
import model.effect.EffectManager._


case class DoesNDmg(baseDmgCount: Int, pokemonToApply: String) extends AttackEffect {
  override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    atkTo(pokemonToApply, defendingBoard.activePokemon, attackingBoard.pokemonBench, defendingBoard.pokemonBench).foreach(pkm =>
      if (pkm == defendingBoard.activePokemon)
        pkm.get.addDamage(totalDmgToEnemyPkm, attackingBoard.activePokemon.get.pokemonTypes)
      else
        pkm.get.addDamage(totalDmgToEnemyPkm, Seq(EnergyType.Colorless)))

    Thread.sleep(2500)
  }

  override var params: Seq[Params] = Seq()
  override var totalDmgToEnemyPkm: Int = totalDmgToEnemyPkm + baseDmgCount
}

sealed trait ForEachEnergyAttachedTo extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[EachEnergyParams]).get.asInstanceOf[EachEnergyParams]
    val pokemonToApply: Option[PokemonCard] = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    val limitedTo = effectParams.limitBy.toInt
    var dmgToAdd = pokemonToApply.get.totalEnergiesStored

    if (limitedTo > 0) {
      dmgToAdd = pokemonToApply.get.totalEnergiesStored - pokemonToApply.get.attacks(effectParams.atkPosition.toInt - 1).cost.size
      if (dmgToAdd > limitedTo)
        dmgToAdd = limitedTo
    }
    totalDmgToEnemyPkm += effectParams.plusDmg.toInt * dmgToAdd
    super.useEffect(attackingBoard, defendingBoard)
  }
}

sealed trait ForEachDamageCount extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[EachDmgParams]).get.asInstanceOf[EachDmgParams]

    val pokemonSelected = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    val dmgCount = pokemonSelected.get.initialHp - pokemonSelected.get.actualHp
    if (effectParams.signature == "+")
      totalDmgToEnemyPkm += dmgCount
    else
      totalDmgToEnemyPkm -= dmgCount
    super.useEffect(attackingBoard, defendingBoard)
  }
}

sealed trait DiscardEnergy extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[DiscardEnergyParams]).get.asInstanceOf[DiscardEnergyParams]
    val pokemonToApply: Option[PokemonCard] = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    var energyCount = effectParams.discardAmount.toInt
    if (energyCount == -1)
      energyCount = pokemonToApply.get.totalEnergiesStored

    effectParams.energyType match {
      case "Colorless" => pokemonToApply.get.removeFirstNEnergies(energyCount)
      case specificEnergy =>
        for (_ <- 1 to energyCount)
          pokemonToApply.get.removeEnergy(EnergyType.withNameWithDefault(specificEnergy))
    }
    super.useEffect(attackingBoard, defendingBoard)
  }
}

sealed trait RecoverLife extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[RecoveryParams]).get.asInstanceOf[RecoveryParams]
    val totalAmount = attackingBoard.activePokemon.get.initialHp - attackingBoard.activePokemon.get.actualHp

    if (effectParams.recoveryAmount.toInt == -1) {
      attackingBoard.activePokemon.get.actualHp += totalAmount
    } else
      attackingBoard.activePokemon.get.actualHp += effectParams.recoveryAmount.toInt
    super.useEffect(attackingBoard, defendingBoard)
  }
}

sealed trait SetImmunity extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    attackingBoard.activePokemon.get.immune = true
    super.useEffect(attackingBoard, defendingBoard)
  }
}

sealed trait MultipleTargetDmg extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[NDmgParams]).last.asInstanceOf[NDmgParams]
    atkTo(effectParams.enemyToAtk, defendingBoard.activePokemon, attackingBoard.pokemonBench.filter(c => c.isDefined), defendingBoard.pokemonBench.filter(c => c.isDefined)).foreach(pkm => pkm.get.addDamage(effectParams.basicDmg.toInt, Seq(EnergyType.Colorless)))
    super.useEffect(attackingBoard, defendingBoard)
  }
}

sealed trait DmgMySelf extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    if (params.exists(p => p.isInstanceOf[DmgMyselfParams])) {
      val effectParamsDmgMyself = params.find(p => p.isInstanceOf[DmgMyselfParams]).last.asInstanceOf[DmgMyselfParams]
      attackingBoard.activePokemon.get.actualHp = attackingBoard.activePokemon.get.actualHp - effectParamsDmgMyself.dmgMyself.toInt
    } else {
      val dmgMyselfOrNot = params.find(p => p.isInstanceOf[DmgMyselfOrNotParams]).last.asInstanceOf[DmgMyselfOrNotParams]
      if (getCoinFlipValue == CoinValue.Head)
        attackingBoard.activePokemon.get.actualHp = attackingBoard.activePokemon.get.actualHp - dmgMyselfOrNot.headDmg.toInt
      else
        attackingBoard.activePokemon.get.actualHp = attackingBoard.activePokemon.get.actualHp - dmgMyselfOrNot.tailDmg.toInt
    }
    super.useEffect(attackingBoard, defendingBoard)
  }
}

sealed trait addStatus extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[StatusParams]).last.asInstanceOf[StatusParams]
    var statusToApply: StatusType = StatusType.withNameWithDefault(effectParams.firstStatusType)

    if (effectParams.firstEffectCoin != "" || effectParams.secondEffectCoin != "")
      if (getCoinFlipValue == CoinValue.Tail)
        statusToApply = StatusType.withNameWithDefault(effectParams.secondStatus)

    val pokemonToApply = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    if (pokemonToApply.get.status == StatusType.NoStatus)
      pokemonToApply.get.status = statusToApply
    super.useEffect(attackingBoard, defendingBoard)
  }
}

class DoesNDmgForEachEnergyAttachedTo(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with ForEachEnergyAttachedTo

class DoesNDmgForEachDamageCount(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with ForEachDamageCount

class DoesNDmgAndDmgMyself(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with DmgMySelf

class DoesNDmgAndDiscardEnergy(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with DiscardEnergy

class DiscardEnergyAndRecover(dmgCount: Int, pokemonToApply: String) extends DoesNDmgAndDiscardEnergy(dmgCount, pokemonToApply) with RecoverLife

class DoesNDmgAndSetImmunity(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with SetImmunity

class DiscardEnergyAndSetImmunity(dmgCount: Int, pokemonToApply: String) extends DoesNDmgAndDiscardEnergy(dmgCount, pokemonToApply) with SetImmunity

class DoesDmgToMultipleTarget(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with MultipleTargetDmg

class DoesDmgAndApplyStatus(dmgCount: Int, pokemonToApply: String ) extends DoesNDmg(dmgCount, pokemonToApply) with addStatus

class DoesDmgToMultipleTarget_AND_DmgMyself(dmgCount: Int, pokemonToApply: String) extends DoesDmgToMultipleTarget(dmgCount, pokemonToApply) with DmgMySelf


private object utils {
  def getAtkOrDef(string: String, attackingPokemon: Option[PokemonCard], defendingPokemon: Option[PokemonCard]): Option[PokemonCard] = {
    string match {
      case "atk" => attackingPokemon
      case "def" => defendingPokemon
    }
  }

  def atkTo(string: String, defendingPokemon: Option[PokemonCard], myBench: Seq[Option[PokemonCard]], enemyBench: Seq[Option[PokemonCard]]): Seq[Option[PokemonCard]] = {
    var seq: Seq[Option[PokemonCard]] = Seq()
    string match {
      case "myBench" => myBench
      case "enemyBench" => enemyBench
      case "single" => Seq(defendingPokemon)
      case "bothBench" =>
        seq = seq ++ enemyBench ++ myBench
        seq
    }
  }
}
