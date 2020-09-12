package model.pokemonEffect

import model.Cards.PokemonCard
import model.{EnergyType, StatusType}
import model.EnergyType.EnergyType
import model.StatusType.statusType
import model.pokemonEffect.staticMethod.{atkTo, getAtkOrDef}


case class DoesNDmg(baseDmgCount: Int, pokemonToApply: String) extends AttackEffect {
  override def useEffect(enemyPokemon: Seq[PokemonCard], myBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    atkTo(pokemonToApply, defendingPokemon, myBench, enemyPokemon).foreach(pkm =>
      if (pkm == defendingPokemon)
        pkm.addDamage(totalDmgToEnemyPkm, attackingPokemon.pokemonTypes)
      else
        pkm.addDamage(totalDmgToEnemyPkm, Seq(EnergyType.colorless)))
  }

  override var args: Map[String, Any] = Map.empty[String, Any]
  override var totalDmgToEnemyPkm: Int = totalDmgToEnemyPkm + baseDmgCount
}

sealed trait ForEachEnergyAttachedTo extends AttackEffect {
  abstract override def useEffect(enemyBench: Seq[PokemonCard], myBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    val pokemonToApply: PokemonCard = getAtkOrDef(getStringArgFromMap("atk_or_def"), attackingPokemon, defendingPokemon)
    val limitedTo = getStringArgFromMap("limited").toInt
    var dmgToAdd = pokemonToApply.totalEnergiesStored
    //es. Blastoise / Poliwrath
    if (limitedTo > 0) {
      dmgToAdd = pokemonToApply.totalEnergiesStored - pokemonToApply.attacks(getStringArgFromMap("attackPosition").toInt - 1).cost.size
      if (dmgToAdd > limitedTo)
        dmgToAdd = limitedTo
    }

    totalDmgToEnemyPkm += getIntArgFromMap("dmgCount") * dmgToAdd

    super.useEffect(enemyBench, myBench, attackingPokemon, defendingPokemon)

  }
}

sealed trait ForEachDamageCount extends AttackEffect {
  abstract override def useEffect(enemyBench: Seq[PokemonCard], myBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    val pokemonSelected = getAtkOrDef(getStringArgFromMap("atk_or_def"), attackingPokemon, defendingPokemon)
    val dmgCount = pokemonSelected.initialHp - pokemonSelected.actualHp
    if (getStringArgFromMap("pluseOrMinus") == "+")
      totalDmgToEnemyPkm += dmgCount
    else
      totalDmgToEnemyPkm -= dmgCount
    super.useEffect(enemyBench, myBench, attackingPokemon, defendingPokemon)
  }
}

sealed trait DiscardEnergy extends AttackEffect {
  abstract override def useEffect(enemyBench: Seq[PokemonCard], myBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    val pokemonToApply: PokemonCard = getAtkOrDef(getStringArgFromMap("atk_or_def"), attackingPokemon, defendingPokemon)
    var energyCount = getIntArgFromMap("energyCount")

    if (energyCount == -1)
      energyCount = pokemonToApply.totalEnergiesStored

    getStringArgFromMap("energyType") match {
      case "Colorless" => pokemonToApply.removeFirstNEnergy(getIntArgFromMap("energyCount"))
      case specificEnergy =>
        for (_ <- 1 to energyCount)
          try {
            pokemonToApply.removeEnergy(EnergyType.withNameWithDefault(specificEnergy))
          }
    }
    super.useEffect(enemyBench, myBench, attackingPokemon, defendingPokemon)
  }
}

sealed trait RecoverLife extends AttackEffect {
  abstract override def useEffect(enemyBench: Seq[PokemonCard], myBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    var recoveryAmount = getIntArgFromMap("recoveryAmount")
    if (recoveryAmount == -1)
      recoveryAmount = attackingPokemon.initialHp - attackingPokemon.actualHp
    val pokemonToApply = getStringArgFromMap("recoveryApplyTo")
    attackingPokemon.actualHp += recoveryAmount

    super.useEffect(enemyBench, myBench, attackingPokemon, defendingPokemon)
  }
}

sealed trait SetImmunity extends AttackEffect {
  abstract override def useEffect(enemyBench: Seq[PokemonCard], myBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    attackingPokemon.immune = true
    super.useEffect(enemyBench, myBench, attackingPokemon, defendingPokemon)
  }
}

sealed trait MultipleTargetDmg extends AttackEffect {
  abstract override def useEffect(enemyBench: Seq[PokemonCard], myBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    val pokemonToApply = getStringArgFromMap("target")
    val dmgToDo = getIntArgFromMap("dmgToMultiple")

    atkTo(pokemonToApply, defendingPokemon, myBench, enemyBench).foreach(pkm => pkm.addDamage(dmgToDo, Seq(EnergyType.colorless)))

    super.useEffect(enemyBench, myBench, attackingPokemon, defendingPokemon)
  }
}

sealed trait DmgMySelf extends AttackEffect {
  abstract override def useEffect(benchPokemon: Seq[PokemonCard], MyBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    attackingPokemon.actualHp = attackingPokemon.actualHp - getIntArgFromMap("DmgMyself")
    super.useEffect(benchPokemon: Seq[PokemonCard], MyBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard)
  }
}

sealed trait addStatus extends AttackEffect {
  abstract override def useEffect(benchPokemon: Seq[PokemonCard], MyBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): Unit = {
    val statusToApply: statusType = StatusType.withNameWithDefault(getStringArgFromMap("status"))
    val pokemonToApply = getAtkOrDef(getStringArgFromMap("atk_or_def"), attackingPokemon, defendingPokemon)
    if (pokemonToApply.status == StatusType.noStatus)
      pokemonToApply.status = statusToApply
    super.useEffect(benchPokemon: Seq[PokemonCard], MyBench: Seq[PokemonCard], attackingPokemon: PokemonCard, defendingPokemon: PokemonCard)
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

class DoesDmgAndApplyStatus(dmgCount: Int, pokemonToApply: String) extends DoesNDmg(dmgCount, pokemonToApply)

class DoesDmgToMultipleTarget_AND_DmgMyself(dmgCount: Int, pokemonToApply: String) extends DoesDmgToMultipleTarget(dmgCount, pokemonToApply) with DmgMySelf


private object staticMethod {

  def getAtkOrDef(string: String, attackingPokemon: PokemonCard, defendingPokemon: PokemonCard): PokemonCard = {
    string match {
      case "atk" => attackingPokemon
      case "def" => defendingPokemon
    }
  }

  def atkTo(string: String, defendingPokemon: PokemonCard, myBench: Seq[PokemonCard], enemyBench: Seq[PokemonCard]): Seq[PokemonCard] = {
    var seq: Seq[PokemonCard] = Seq()
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
