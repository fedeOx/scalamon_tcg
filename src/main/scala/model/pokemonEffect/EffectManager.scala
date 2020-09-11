package model.pokemonEffect

import scala.util.Random

object EffectManager {

  def convertJsonEffectToAttackEffect(jsonEffect: Option[Seq[Effect]]): Option[AttackEffect] = {

    val mainEffect: Effect = jsonEffect.get.head
    mainEffect.name match {
      case EffectType.doesNdmg => Some(doesNDmgEffectSpecialize(jsonEffect.get))
      case EffectType.discardEnergy => None
      case EffectType.status => None
      case _ => None
    }
  }

  private def doesNDmgEffectSpecialize(jsonEffect: Seq[Effect]): AttackEffect = {
    val basicEffectParams = jsonEffect.head.params
    var basicDmgToDo = basicEffectParams(0).toInt
    val basicCoinFlipNumber = basicEffectParams(1).toInt
    val basicCoinSide = basicEffectParams(2)

    var returnedAttack: AttackEffect = null

    def resolveAttack(attackEffect: Seq[Effect]): Unit = attackEffect match {
      case h :: t if h.name == EffectType.doesNdmg && t.isEmpty => {
        if (basicCoinSide != "") {
          //do dmg according to Coin Value (es. Beedrill)
          var newBasicDmgToDo = 0

          @scala.annotation.tailrec
          def modifyDmgCount(iterationLeft: Int): Int = iterationLeft match {
            case 0 => newBasicDmgToDo
            case _ => {
              if (getCoinFlipValue() == basicCoinSide)
                newBasicDmgToDo += basicDmgToDo

              modifyDmgCount(iterationLeft - 1)
            }
          }
          basicDmgToDo = modifyDmgCount(basicCoinFlipNumber)
        }

        returnedAttack = returnedEffect(DoesNDmg(basicDmgToDo))
      }
      //Blastoise (atk) , //MewTwo (def)
      case h :: t if h.name == EffectType.eachEnergy && t.isEmpty => {
        returnedAttack = returnedEffect(new DoesNDmgForEachEnergyAttachedTo(basicDmgToDo),
          "dmgCount" -> h.params.head.toInt,
          "atk_or_def" -> h.params(1) ,
          "limited" -> Some(h.params(2)).getOrElse(0) ,
          "attackPosition"-> Some(h.params(3)).getOrElse(0))
      }
      case h :: t if h.name == EffectType.eachDmg && t.isEmpty => {
        returnedAttack = returnedEffect(new DoesNDmgForEachDamageCount(basicDmgToDo))
      }
      case h :: t if (h.name == EffectType.doesNDmgAndHitMyself_OR_doesNdmg || h.name == EffectType.doesNDmgAndHitMyself) && t.isEmpty => {
        //default tail -> dmgMyself
        returnedAttack = returnedEffect(new DoesNDmgAndDmgMyself(basicDmgToDo), "DmgMyself" -> h.params.head.toInt)

        if (h.name == EffectType.doesNDmgAndHitMyself_OR_doesNdmg)
          if (getCoinFlipValue == "head")
            returnedAttack = returnedEffect(new DoesNDmgAndDmgMyself(basicDmgToDo + h.params(1).toInt), "DmgMyself" -> 0)
      }
      case _ :: t => resolveAttack(t)
    }

    resolveAttack(jsonEffect)
    returnedAttack
  }

  private def returnedEffect(item: DoesNDmg, tuple1: (String, Any)*): AttackEffect = {
    tuple1.foreach(element => item.args += element)
    item
  }

  private def doesNDmgAndHitMyself(basicDmgToDo: Int, seq: Effect): AttackEffect = {
    returnedEffect(new DoesNDmgAndDmgMyself(basicDmgToDo), "DmgMyself" -> seq.params.head.toInt)

  }

  private def getCoinFlipValue(): String = {
    if (Random.nextInt(99) + 1 < 50) {
      println("head")
      return "head"
    }

    println("tail")
    "tail"
  }

  private def discardEnergySpecialize(jsonEffect: Seq[Effect]): AttackEffect = {
    var returnedAttack: AttackEffect = null
    returnedAttack
  }

}


