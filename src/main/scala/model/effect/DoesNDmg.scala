package model.effect

import common.CoinUtil
import common.CoinUtil.CoinValue
import controller.Controller
import model.core.GameManager
import model.game.Cards.PokemonCard
import model.game.{Board, EnergyType, StatusType}
import model.game.StatusType.StatusType
import model.effect.utils.{atkTo, getAtkOrDef}
import model.event.Events.Event


case class DoesNDmg(baseDmgCount: Int, pokemonToApply: String) extends AttackEffect {
  override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    atkTo(pokemonToApply, defendingBoard.activePokemon, attackingBoard.pokemonBench, defendingBoard.pokemonBench).foreach(pkm =>
      if (pkm == defendingBoard.activePokemon)
        pkm.get.addDamage(totalDmgToEnemyPkm - pkm.get.damageModifier, attackingBoard.activePokemon.get.pokemonTypes)
      else
        pkm.get.addDamage(totalDmgToEnemyPkm - pkm.get.damageModifier, Seq(EnergyType.Colorless)))

    Thread.sleep(2500)
  }

  override var params: Seq[Params] = Seq()
  override var totalDmgToEnemyPkm: Int = totalDmgToEnemyPkm + baseDmgCount
}

sealed trait ForEachEnergyAttachedTo extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[EachEnergyParams]).get.asInstanceOf[EachEnergyParams]
    val pokemonToApply: Option[PokemonCard] = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    val limitedTo = effectParams.limitBy
    var dmgToAdd = pokemonToApply.get.totalEnergiesStored

    if (limitedTo > 0) {
      dmgToAdd = pokemonToApply.get.totalEnergiesStored - pokemonToApply.get.attacks(effectParams.atkPosition - 1).cost.size
      if (dmgToAdd > limitedTo)
        dmgToAdd = limitedTo
    }
    totalDmgToEnemyPkm += effectParams.plusDmg * dmgToAdd
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait ForEachDamageCount extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[EachDmgParams]).get.asInstanceOf[EachDmgParams]
    val pokemonSelected = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    val dmgCount = pokemonSelected.get.initialHp - pokemonSelected.get.actualHp
    if (effectParams.signature == "+")
      totalDmgToEnemyPkm += dmgCount
    else
      totalDmgToEnemyPkm -= dmgCount
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait DiscardEnergy extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[DiscardEnergyParams]).get.asInstanceOf[DiscardEnergyParams]
    val pokemonToApply: Option[PokemonCard] = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    var energyCount = effectParams.discardAmount
    if (energyCount == -1)
      energyCount = pokemonToApply.get.totalEnergiesStored
    effectParams.energyType match {
      case EnergyType.Colorless => pokemonToApply.get.removeFirstNEnergies(energyCount)
      case specificEnergy =>
        for (_ <- 1 to energyCount)
          pokemonToApply.get.removeEnergy(specificEnergy)
    }
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait RecoverLife extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[RecoveryParams]).get.asInstanceOf[RecoveryParams]
    val totalAmount = attackingBoard.activePokemon.get.initialHp - attackingBoard.activePokemon.get.actualHp
    if (effectParams.recoveryAmount == -1) {
      attackingBoard.activePokemon.get.actualHp += totalAmount
    } else
      attackingBoard.activePokemon.get.actualHp += effectParams.recoveryAmount
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait SetImmunity extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    attackingBoard.activePokemon.get.immune = true
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait MultipleTargetDmg extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[ToBenchParams]).last.asInstanceOf[ToBenchParams]
    var benchToApply = ""
    //case dmg to bench without coinflip
    if (effectParams.headDmgTo == "" && effectParams.tailDmgTo == "")
      benchToApply = effectParams.benchToApply
    else {
      if (CoinUtil.flipACoin() == CoinValue.Head)
        benchToApply = effectParams.headDmgTo
      else
        benchToApply = effectParams.tailDmgTo
    }
    val pokemonToDoDmg: Seq[Option[PokemonCard]] = atkTo(benchToApply, defendingBoard.activePokemon, attackingBoard.pokemonBench.filter(c => c.isDefined), defendingBoard.pokemonBench.filter(c => c.isDefined))

    var numberOfPokemonToApply: Int = effectParams.limit
    if (numberOfPokemonToApply == -1) {
      numberOfPokemonToApply = pokemonToDoDmg.size
      for (i <- 0 until numberOfPokemonToApply)
        pokemonToDoDmg(i).get.addDamage(effectParams.dmgToDo, Seq(EnergyType.Colorless))
    } else if (defendingBoard.pokemonBench.count(c => c.isDefined) > 0)
      gameManager.notifyObservers(Event.damageBenchEffect(numberOfPokemonToApply, effectParams.dmgToDo))

    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait DmgMySelf extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    if (params.exists(p => p.isInstanceOf[DmgMyselfParams])) {
      val effectParamsDmgMyself = params.find(p => p.isInstanceOf[DmgMyselfParams]).last.asInstanceOf[DmgMyselfParams]
      attackingBoard.activePokemon.get.addDamage(effectParamsDmgMyself.dmgMyself, Seq(EnergyType.Colorless))
    } else {
      val dmgMyselfOrNot = params.find(p => p.isInstanceOf[DmgMyselfOrNotParams]).last.asInstanceOf[DmgMyselfOrNotParams]
      if (CoinUtil.flipACoin() == CoinValue.Head)
        attackingBoard.activePokemon.get.addDamage(dmgMyselfOrNot.headDmg, Seq(EnergyType.Colorless))
      else
        attackingBoard.activePokemon.get.addDamage(dmgMyselfOrNot.tailDmg, Seq(EnergyType.Colorless))
    }
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait addStatus extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[StatusParams]).last.asInstanceOf[StatusParams]
    var statusToApply: StatusType = effectParams.firstStatusType

    if (effectParams.firstEffectCoin != "" || effectParams.secondEffectCoin != "")
      if (CoinUtil.flipACoin() == CoinValue.Tail)
        statusToApply = effectParams.secondStatus

    val pokemonToApply = getAtkOrDef(effectParams.pokemonToApply, attackingBoard.activePokemon, defendingBoard.activePokemon)
    if (pokemonToApply.get.status == StatusType.NoStatus)
      pokemonToApply.get.status = statusToApply
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

sealed trait PreventDmg extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    val effectParams = params.find(p => p.isInstanceOf[PreventParams]).get.asInstanceOf[PreventParams]
    attackingBoard.activePokemon.get.damageModifier = effectParams.dmgToPrevent
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

class Recovery(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with RecoverLife

class Prevent(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with PreventDmg

class DoesNDmgForEachEnergyAttachedTo(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with ForEachEnergyAttachedTo

class DoesNDmgForEachDamageCount(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with ForEachDamageCount

class DoesNDmgAndDmgMyself(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with DmgMySelf

class DoesNDmgAndDiscardEnergy(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with DiscardEnergy

class DiscardEnergyAndRecover(dmgCount: Int, pokemonToApply: String) extends DoesNDmgAndDiscardEnergy(dmgCount, pokemonToApply) with RecoverLife

class DoesNDmgAndSetImmunity(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with SetImmunity

class DiscardEnergyAndSetImmunity(dmgCount: Int, pokemonToApply: String) extends DoesNDmgAndDiscardEnergy(dmgCount, pokemonToApply) with SetImmunity

class DoesDmgToMultipleTarget(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with MultipleTargetDmg

class DoesDmgAndApplyStatus(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with addStatus

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
      case "atk" => myBench
      case "def" => enemyBench
      case "single" => Seq(defendingPokemon)
      case "both" =>
        seq = seq ++ enemyBench ++ myBench
        seq
    }
  }
}
