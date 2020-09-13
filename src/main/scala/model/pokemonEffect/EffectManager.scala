package model.pokemonEffect


import scala.util.Random

object EffectManager {

  def convertJsonEffectToAttackEffect(jsonEffect: Option[Seq[Effect]]): Option[AttackEffect] = {
    val mainEffect: Effect = jsonEffect.get.head
    mainEffect.name match {
      case EffectType.doesNdmg => Some(doesNDmgEffectSpecialize(jsonEffect.get))
      case EffectType.trainer => None
      case EffectType.pokePower => None
      case _ => None
    }
  }

  private def doesNDmgEffectSpecialize(jsonEffect: Seq[Effect]): AttackEffect = {
    val basicEffectParams = jsonEffect.head.params
    var basicDmgToDo = basicEffectParams.head.toInt
    val basicCoinFlipNumber = basicEffectParams(1).toInt
    val basicCoinSide = basicEffectParams(2)
    val basicEnemyToAtk = basicEffectParams(3)
    var returnedAttack: AttackEffect = null
    var effectArgs: Map[String, Any] = Map.empty

    @scala.annotation.tailrec
    def resolveAttack(attackEffect: Seq[Effect]): Unit = attackEffect match {
      //base Atk dmg
      case h :: t if h.name == EffectType.doesNdmg && t.isEmpty => {
        if (basicCoinSide != "") {
          //do dmg according to Coin Value
          var newBasicDmgToDo = 0
          @scala.annotation.tailrec
          def modifyDmgCount(iterationLeft: Int): Int = iterationLeft match {
            case 0 => newBasicDmgToDo
            case _ =>
              if (getCoinFlipValue == basicCoinSide)
                newBasicDmgToDo += basicDmgToDo

              modifyDmgCount(iterationLeft - 1)
          }

          basicDmgToDo = modifyDmgCount(basicCoinFlipNumber)
        }
        returnedAttack = returnedEffect(DoesNDmg(basicDmgToDo, basicEnemyToAtk), effectArgs)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //base Atk dmg + plus Dmg foreach Energy
      case h :: t if h.name == EffectType.eachEnergy => {
        effectArgs += ("dmgCount" -> h.params.head.toInt)
        effectArgs += ("atk_or_def" -> h.params(1))
        effectArgs += ("limited" -> Some(h.params(2)).getOrElse(0))
        effectArgs += ("attackPosition" -> Some(h.params(3)).getOrElse(0))

        returnedAttack = returnedEffect(new DoesNDmgForEachEnergyAttachedTo(basicDmgToDo, basicEnemyToAtk), effectArgs)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //base Atk dmg + plus Dmg foreach dmg
      case h :: t if h.name == EffectType.eachDmg => {
        effectArgs += ("PlusDmg" -> h.params.head.toInt)
        effectArgs += ("atk_or_def" -> h.params(1))
        effectArgs += ("plusOrMinus" -> h.params(2))
        returnedAttack = returnedEffect(new DoesNDmgForEachDamageCount(basicDmgToDo, basicEnemyToAtk), effectArgs)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //Base Atk dmg + dmg Myself or Use CoinFlip for decide it
      case h :: t if (h.name == EffectType.doesNDmgAndHitMyself_OR_doesNdmg || h.name == EffectType.doesNDmgAndHitMyself) && t.isEmpty => {
        if (returnedAttack == null) {
          //default tail -> dmgMyself
          effectArgs += ("DmgMyself" -> h.params.head.toInt)
          returnedAttack = returnedEffect(new DoesNDmgAndDmgMyself(basicDmgToDo, basicEnemyToAtk), effectArgs)
          if (h.name == EffectType.doesNDmgAndHitMyself_OR_doesNdmg && getCoinFlipValue == "head") {
            effectArgs += ("DmgMyself" -> h.params.head.toInt)
            returnedAttack = returnedEffect(new DoesNDmgAndDmgMyself(basicDmgToDo + h.params(1).toInt, basicEnemyToAtk), effectArgs)
          }
        } else {
          effectArgs += ("DmgMyself" -> h.params.head.toInt)
          returnedAttack = returnedEffect(new DoesDmgToMultipleTarget_AND_DmgMyself(basicDmgToDo, basicEnemyToAtk), effectArgs)
        }
      }
      //Base atk dmg and discard Energy
      case h :: t if h.name == EffectType.discardEnergy => {
        effectArgs += ("energyCount" -> h.params.head.toInt)
        effectArgs += ("energyType" -> h.params(1))
        effectArgs += ("atk_or_def" -> h.params(2))
        returnedAttack = returnedEffect(new DoesNDmgAndDiscardEnergy(basicDmgToDo, basicEnemyToAtk), effectArgs)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //recover Life
      case h :: t if h.name == EffectType.recovery => {
        effectArgs += ("recoveryAmount" -> h.params.head.toInt)
        effectArgs += ("recoveryApplyTo" -> h.params(1))
        returnedAttack = returnedEffect(new DiscardEnergyAndRecover(basicDmgToDo, basicEnemyToAtk), effectArgs)
      }
      //Base atk dmg and set Immunity for the next turn
      case h :: t if h.name == EffectType.doesNDmgAndSetImmunity => {
        val isTailBounded: String = h.params.head
        val isHeadBounded: String = h.params(1)
        if (t.isEmpty) {
          if ((isHeadBounded == "true" && getCoinFlipValue == "head") || (isTailBounded == "true" && getCoinFlipValue == "tail") || (isTailBounded == "" && isHeadBounded == ""))
            returnedAttack = returnedEffect(new DoesNDmgAndSetImmunity(basicDmgToDo, basicEnemyToAtk), effectArgs)
          else
            returnedAttack = returnedEffect(DoesNDmg(basicDmgToDo, basicEnemyToAtk), effectArgs)
        } else {
          effectArgs += ("energyCount" -> t.head.params.head.toInt)
          effectArgs += ("energyType" -> t.head.params(1))
          effectArgs += ("atk_or_def" -> t.head.params(2))
          returnedAttack = returnedEffect(new DiscardEnergyAndSetImmunity(basicDmgToDo, basicEnemyToAtk), effectArgs)
        }
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //Base atk to defending pokemon + Dmg to Bench ( Multiple Dmg)
      case h :: t if h.name == EffectType.doesNdmg && t.head.name == EffectType.doesNdmg => {
        effectArgs += ("dmgToMultiple" -> t.head.params.head.toInt)
        effectArgs += ("target" -> t.head.params(3))
        returnedAttack = returnedEffect(new DoesDmgToMultipleTarget(basicDmgToDo, basicEnemyToAtk), effectArgs)
          resolveAttack(t.tail)
      }
      //Status
      case h :: t if h.name == EffectType.status => {
        val isCoinNeeded = h.params(2) != "" || h.params(3) != ""
        effectArgs += ("atk_or_def" -> h.params.head)
        effectArgs += ("status" -> h.params(1))
        if (isCoinNeeded)
          if (getCoinFlipValue == "tail")
            effectArgs += ("status" -> h.params(4))


        returnedAttack = returnedEffect(new DoesDmgAndApplyStatus(basicDmgToDo, basicEnemyToAtk), effectArgs)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      case _ :: t => resolveAttack(t)
    }

    resolveAttack(jsonEffect)
    returnedAttack
  }

  private def returnedEffect(item: DoesNDmg, map: Map[String, Any]): AttackEffect = {
    item.args = map
    item
  }


  private def getCoinFlipValue: String = {
    Random.nextInt(99) + 1 match {
      case n if n<= 50 => "head"
      case _ => "tail"
    }
  }


}


