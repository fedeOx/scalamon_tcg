package model.pokemonEffect

import model.game.Cards.PokemonCard
import model.game.{Board, EnergyType, StatusType}
import model.game.StatusType.StatusType
import model.pokemonEffect.staticMethod.{atkTo, getAtkOrDef}


case class DoesNDmg(baseDmgCount: Int, pokemonToApply: String) extends AttackEffect {
  override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    atkTo(pokemonToApply, defendingBoard.activePokemon, attackingBoard.pokemonBench , defendingBoard.pokemonBench).foreach(pkm =>
      if (pkm == defendingBoard.activePokemon)
        pkm.get.addDamage(totalDmgToEnemyPkm, attackingBoard.activePokemon.get.pokemonTypes)
      else
        pkm.get.addDamage(totalDmgToEnemyPkm, Seq(EnergyType.Colorless)))
  }
  override var args: Map[String, Any] = Map.empty[String, Any]
  override var totalDmgToEnemyPkm: Int = totalDmgToEnemyPkm + baseDmgCount
}

sealed trait ForEachEnergyAttachedTo extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    val pokemonToApply: Option[PokemonCard] = getAtkOrDef(getStringArgFromMap("atk_or_def"), attackingBoard.activePokemon, defendingBoard.activePokemon)
    val limitedTo = getStringArgFromMap("limited").toInt
    var dmgToAdd = pokemonToApply.get.totalEnergiesStored

    if (limitedTo > 0) {
      dmgToAdd = pokemonToApply.get.totalEnergiesStored - pokemonToApply.get.attacks(getStringArgFromMap("attackPosition").toInt - 1).cost.size
      if (dmgToAdd > limitedTo)
        dmgToAdd = limitedTo
    }
    totalDmgToEnemyPkm += getIntArgFromMap("dmgCount") * dmgToAdd
    super.useEffect(attackingBoard,defendingBoard)
  }
}

sealed trait ForEachDamageCount extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    val pokemonSelected = getAtkOrDef(getStringArgFromMap("atk_or_def"), attackingBoard.activePokemon, defendingBoard.activePokemon)
    val dmgCount = pokemonSelected.get.initialHp - pokemonSelected.get.actualHp
    if (getStringArgFromMap("plusOrMinus") == "+")
      totalDmgToEnemyPkm += dmgCount
    else
      totalDmgToEnemyPkm -= dmgCount
    super.useEffect(attackingBoard,defendingBoard)
  }
}

sealed trait DiscardEnergy extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    val pokemonToApply: Option[PokemonCard] = getAtkOrDef(getStringArgFromMap("atk_or_def"),attackingBoard.activePokemon, defendingBoard.activePokemon)
    var energyCount = getIntArgFromMap("energyCount")

    if (energyCount == -1)
      energyCount = pokemonToApply.get.totalEnergiesStored
    getStringArgFromMap("energyType") match {
      case "Colorless" => pokemonToApply.get.removeFirstNEnergies(getIntArgFromMap("energyCount"))
      case specificEnergy =>
        for (_ <- 1 to energyCount)
            pokemonToApply.get.removeEnergy(EnergyType.withNameWithDefault(specificEnergy))
    }
    super.useEffect(attackingBoard,defendingBoard)
  }
}

sealed trait RecoverLife extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    var recoveryAmount = getIntArgFromMap("recoveryAmount")

    if (recoveryAmount == -1)
      recoveryAmount = attackingBoard.activePokemon.get.initialHp - attackingBoard.activePokemon.get.actualHp
    val pokemonToApply = getStringArgFromMap("recoveryApplyTo")
    attackingBoard.activePokemon.get.actualHp += recoveryAmount
    super.useEffect(attackingBoard,defendingBoard)
  }
}

sealed trait SetImmunity extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    attackingBoard.activePokemon.get.immune = true
    super.useEffect(attackingBoard,defendingBoard)
  }
}

sealed trait MultipleTargetDmg extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    val pokemonToApply = getStringArgFromMap("target")
    val dmgToDo = getIntArgFromMap("dmgToMultiple")

    atkTo(pokemonToApply, defendingBoard.activePokemon, attackingBoard.pokemonBench, defendingBoard.pokemonBench).foreach(pkm => pkm.get.addDamage(dmgToDo, Seq(EnergyType.Colorless)))
    super.useEffect(attackingBoard,defendingBoard)
  }
}

sealed trait DmgMySelf extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    attackingBoard.activePokemon.get.actualHp = attackingBoard.activePokemon.get.actualHp - getIntArgFromMap("DmgMyself")
    super.useEffect(attackingBoard,defendingBoard)
  }
}

sealed trait addStatus extends AttackEffect {
  abstract override def useEffect(attackingBoard:Board , defendingBoard:Board): Unit = {
    val statusToApply: StatusType = StatusType.withNameWithDefault(getStringArgFromMap("status"))
    val pokemonToApply = getAtkOrDef(getStringArgFromMap("atk_or_def"), attackingBoard.activePokemon, defendingBoard.activePokemon)
    if (pokemonToApply.get.status == StatusType.NoStatus)
      pokemonToApply.get.status = statusToApply
    super.useEffect(attackingBoard,defendingBoard)
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

class DoesDmgAndApplyStatus(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply) with addStatus

class DoesDmgToMultipleTarget_AND_DmgMyself(dmgCount: Int, pokemonToApply: String) extends DoesDmgToMultipleTarget(dmgCount, pokemonToApply) with DmgMySelf


private object staticMethod {

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
      case "bothBench" => {
        seq = seq ++ enemyBench ++ myBench
        seq
      }
    }
  }
}
