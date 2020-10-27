package model.effect

import common.CoinUtil
import common.CoinUtil.CoinValue
import model.card.PokemonCard
import model.core.GameManager
import model.game.{Board, EnergyType, StatusType}
import model.game.StatusType.StatusType
import model.effect.utils.{atkTo, getAtkOrDef}
import model.event.Events.DamageBenchEvent

/**
 * Base class for assigning damage to a pokemon
 *
 * @param baseDmgCount   the amount of damage to be performed
 * @param pokemonToApply represents the Pokémon to attribute the damage to
 */
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

/**
 * Adds damage based on the energy assigned to a specific pokemon
 */
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

/**
 * Adds damage based on the damage received by a specific pokemon
 */
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

/**
 * Removes a number of energy from a pokemon
 */
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

/**
 * Recover a certain amount of life from the selected pokemon
 */
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

/**
 * Sets the immunity for a specific pokemon
 */
sealed trait SetImmunity extends AttackEffect {
  abstract override def useEffect(attackingBoard: Board, defendingBoard: Board, gameManager: GameManager): Unit = {
    attackingBoard.activePokemon.get.immune = true
    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

/**
 * Does damage to a specified number of pokemon
 */
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
      gameManager.notifyObservers(DamageBenchEvent(numberOfPokemonToApply, effectParams.dmgToDo))

    super.useEffect(attackingBoard, defendingBoard, gameManager)
  }
}

/**
 * The attacking pokemon damages itself
 */
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

/**
 * Adds a status to the designated pokemon
 */
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

/**
 * Prevents damage to the pokemon
 */
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
  /**
   * Specifies the target pokemon
   * @param pokemonToApply contains the string within the parameters indicating the target pokemon
   * @param attackingPokemon indicates the current attacking pokemon
   * @param defendingPokemon indicates the current defending pokemon
   * @return the pokemon specified by the parameter
   */
  def getAtkOrDef(pokemonToApply: String, attackingPokemon: Option[PokemonCard], defendingPokemon: Option[PokemonCard]): Option[PokemonCard] = {
    pokemonToApply match {
      case "atk" => attackingPokemon
      case "def" => defendingPokemon
    }
  }

  /**
   * Specifies a sequence of target pokemon
   * @param pokemonToApply contains the string within the parameters indicating the target pokemon
   * @param defendingPokemon  the current defending pokemon
   * @param myBench pokemon on my bench
   * @param enemyBench  the pokemon on the opponent's bench
   * @return
   */
  def atkTo(pokemonToApply: String, defendingPokemon: Option[PokemonCard], myBench: Seq[Option[PokemonCard]], enemyBench: Seq[Option[PokemonCard]]): Seq[Option[PokemonCard]] = {
    var seq: Seq[Option[PokemonCard]] = Seq()
    pokemonToApply match {
      case "atk" => myBench
      case "def" => enemyBench
      case "single" => Seq(defendingPokemon)
      case "both" =>
        seq = seq ++ enemyBench ++ myBench
        seq
    }
  }
}
