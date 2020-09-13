package model.pokemonEffect

import model.game.Cards.PokemonCard
import model.game.EnergyType
import model.game.StatusType
import model.game.StatusType.StatusType
import model.pokemonEffect.staticMethod.{atkTo, getAtkOrDef}


case class DoesNDmg(baseDmgCount: Int, pokemonToApply: String) extends AttackEffect {
  override def useEffect(): Unit = {
    atkTo(pokemonToApply, BoardTmp.defendingPokemon, BoardTmp.myBench, BoardTmp.enemyBench).foreach(pkm =>
      if (pkm == BoardTmp.defendingPokemon)
        pkm.addDamage(totalDmgToEnemyPkm, BoardTmp.activePokemon.pokemonTypes)
      else
        pkm.addDamage(totalDmgToEnemyPkm, Seq(EnergyType.Colorless)))
  }
  override var args: Map[String, Any] = Map.empty[String, Any]
  override var totalDmgToEnemyPkm: Int = totalDmgToEnemyPkm + baseDmgCount
}

sealed trait ForEachEnergyAttachedTo extends AttackEffect {
  abstract override def useEffect(): Unit = {
    val pokemonToApply: PokemonCard = getAtkOrDef(getStringArgFromMap("atk_or_def"), BoardTmp.activePokemon, BoardTmp.defendingPokemon)
    val limitedTo = getStringArgFromMap("limited").toInt
    var dmgToAdd = pokemonToApply.totalEnergiesStored

    if (limitedTo > 0) {
      dmgToAdd = pokemonToApply.totalEnergiesStored - pokemonToApply.attacks(getStringArgFromMap("attackPosition").toInt - 1).cost.size
      if (dmgToAdd > limitedTo)
        dmgToAdd = limitedTo
    }
    totalDmgToEnemyPkm += getIntArgFromMap("dmgCount") * dmgToAdd
    super.useEffect()
  }
}

sealed trait ForEachDamageCount extends AttackEffect {
  abstract override def useEffect(): Unit = {
    val pokemonSelected = getAtkOrDef(getStringArgFromMap("atk_or_def"), BoardTmp.activePokemon, BoardTmp.defendingPokemon)
    val dmgCount = pokemonSelected.initialHp - pokemonSelected.actualHp
    if (getStringArgFromMap("plusOrMinus") == "+")
      totalDmgToEnemyPkm += dmgCount
    else
      totalDmgToEnemyPkm -= dmgCount
    super.useEffect()
  }
}

sealed trait DiscardEnergy extends AttackEffect {
  abstract override def useEffect(): Unit = {
    val pokemonToApply: PokemonCard = getAtkOrDef(getStringArgFromMap("atk_or_def"), BoardTmp.activePokemon, BoardTmp.defendingPokemon)
    var energyCount = getIntArgFromMap("energyCount")

    if (energyCount == -1)
      energyCount = pokemonToApply.totalEnergiesStored
    getStringArgFromMap("energyType") match {
      case "Colorless" => pokemonToApply.removeFirstNEnergy(getIntArgFromMap("energyCount"))
      case specificEnergy =>
        for (_ <- 1 to energyCount)
            pokemonToApply.removeEnergy(EnergyType.withNameWithDefault(specificEnergy))
    }
    super.useEffect()
  }
}

sealed trait RecoverLife extends AttackEffect {
  abstract override def useEffect(): Unit = {
    var recoveryAmount = getIntArgFromMap("recoveryAmount")

    if (recoveryAmount == -1)
      recoveryAmount = BoardTmp.activePokemon.initialHp - BoardTmp.activePokemon.actualHp
    val pokemonToApply = getStringArgFromMap("recoveryApplyTo")
    BoardTmp.activePokemon.actualHp += recoveryAmount
    super.useEffect()
  }
}

sealed trait SetImmunity extends AttackEffect {
  abstract override def useEffect(): Unit = {
    BoardTmp.activePokemon.immune = true
    super.useEffect()
  }
}

sealed trait MultipleTargetDmg extends AttackEffect {
  abstract override def useEffect(): Unit = {
    val pokemonToApply = getStringArgFromMap("target")
    val dmgToDo = getIntArgFromMap("dmgToMultiple")

    atkTo(pokemonToApply, BoardTmp.defendingPokemon, BoardTmp.myBench, BoardTmp.enemyBench).foreach(pkm => pkm.addDamage(dmgToDo, Seq(EnergyType.Colorless)))
    super.useEffect()
  }
}

sealed trait DmgMySelf extends AttackEffect {
  abstract override def useEffect(): Unit = {
    BoardTmp.activePokemon.actualHp = BoardTmp.activePokemon.actualHp - getIntArgFromMap("DmgMyself")
    super.useEffect()
  }
}

sealed trait addStatus extends AttackEffect {
  abstract override def useEffect(): Unit = {
    val statusToApply: StatusType = StatusType.withNameWithDefault(getStringArgFromMap("status"))
    val pokemonToApply = getAtkOrDef(getStringArgFromMap("atk_or_def"), BoardTmp.activePokemon, BoardTmp.defendingPokemon)
    if (pokemonToApply.status == StatusType.NoStatus)
      pokemonToApply.status = statusToApply
    super.useEffect()
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
