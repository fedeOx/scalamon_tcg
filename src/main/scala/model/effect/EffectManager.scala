package model.effect
import common.CoinUtil
import common.CoinUtil.CoinValue
import common.CoinUtil.CoinValue.CoinValue
import model.event.Events.Event

object EffectManager {
  private var totalParamsSeq :Seq[Params] = Seq()

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
    totalParamsSeq = Seq()
    val effectParams = jsonEffect.head.params.head.asInstanceOf[NDmgParams]
    var basicDmgToDo = effectParams.basicDmg.toInt
    val basicCoinFlipNumber = effectParams.coinFlipNumber.toInt
    val basicCoinSide = effectParams.coinSide
    val basicEnemyToAtk = effectParams.enemyToAtk
    var returnedAttack: AttackEffect = null

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
              if (CoinUtil.flipACoin().toString == basicCoinSide)
                newBasicDmgToDo += basicDmgToDo
              modifyDmgCount(iterationLeft - 1)
          }
          basicDmgToDo = modifyDmgCount(basicCoinFlipNumber)
        }
        returnedAttack = returnedEffect(DoesNDmg(basicDmgToDo, basicEnemyToAtk), effectParams)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //base Atk dmg + plus Dmg foreach Energy
      case h :: t if h.name == EffectType.eachEnergy => {
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.eachEnergy).get.params.head.asInstanceOf[EachEnergyParams]
        returnedAttack = returnedEffect(new DoesNDmgForEachEnergyAttachedTo(basicDmgToDo, basicEnemyToAtk), effectParams)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //base Atk dmg + plus Dmg foreach dmg
      case h :: t if h.name == EffectType.eachDmg => {
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.eachDmg).get.params.head.asInstanceOf[EachDmgParams]
        returnedAttack = returnedEffect(new DoesNDmgForEachDamageCount(basicDmgToDo, basicEnemyToAtk), effectParams)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //Base Atk dmg + dmg Myself or Use CoinFlip for decide it
      case h :: t if (h.name == EffectType.doesNDmgAndHitMyself) && t.isEmpty => {
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.doesNDmgAndHitMyself).get.params.head.asInstanceOf[DmgMyselfParams]
        if (returnedAttack == null) {
          returnedAttack = returnedEffect(new DoesNDmgAndDmgMyself(basicDmgToDo, basicEnemyToAtk), effectParams)
        } else {
          returnedAttack = returnedEffect(new DoesDmgToMultipleTarget_AND_DmgMyself(basicDmgToDo, basicEnemyToAtk), effectParams)
        }
      }
      case h :: t if h.name == EffectType.doesNDmgAndHitMyself_OR_doesNdmg => {
        //TODO duplicate code
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.doesNDmgAndHitMyself_OR_doesNdmg).get.params.head.asInstanceOf[DmgMyselfOrNotParams]
          returnedAttack = returnedEffect(new DoesNDmgAndDmgMyself(basicDmgToDo, basicEnemyToAtk), effectParams)
      }
      //Base atk dmg and discard Energy
      case h :: t if h.name == EffectType.discardEnergy => {
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.discardEnergy).get.params.head.asInstanceOf[DiscardEnergyParams]
        returnedAttack = returnedEffect(new DoesNDmgAndDiscardEnergy(basicDmgToDo, basicEnemyToAtk), effectParams)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //recover Life
      case h :: t if h.name == EffectType.recovery => {
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.recovery).get.params.head.asInstanceOf[RecoveryParams]
        returnedAttack = returnedEffect(new DiscardEnergyAndRecover(basicDmgToDo, basicEnemyToAtk), effectParams)
      }
      //Base atk dmg and set Immunity for the next turn
      case h :: t if h.name == EffectType.doesNDmgAndSetImmunity => {
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.doesNDmgAndSetImmunity).get.params.head.asInstanceOf[SetImmunityParams]
        val isTailBounded: String = effectParams.tailBounded
        val isHeadBounded: String = effectParams.headBounded
        if (t.isEmpty) {
          if ((isHeadBounded == "true" && CoinUtil.flipACoin() == CoinValue.Head) || (isTailBounded == "true" &&  CoinUtil.flipACoin() == CoinValue.Tail) || (isTailBounded == "" && isHeadBounded == ""))
            returnedAttack = returnedEffect(new DoesNDmgAndSetImmunity(basicDmgToDo, basicEnemyToAtk), effectParams)
          else
            returnedAttack = returnedEffect(DoesNDmg(basicDmgToDo, basicEnemyToAtk), effectParams)
        } else {
          returnedAttack = returnedEffect(new DiscardEnergyAndSetImmunity(basicDmgToDo, basicEnemyToAtk), effectParams)
        }
        if (t.nonEmpty)
          resolveAttack(t)
      }
      //Base atk to defending pokemon + Dmg to BenchMultipleTargetDmg ( Multiple Dmg)
      case h :: t if h.name == EffectType.doesNdmg && t.head.name == EffectType.doesNdmg => {
        val effectParams = jsonEffect.filter(effect => effect.name == EffectType.doesNdmg).last.params.head.asInstanceOf[NDmgParams]
        returnedAttack = returnedEffect(new DoesDmgToMultipleTarget(basicDmgToDo, basicEnemyToAtk), effectParams)
        resolveAttack(t.tail)
      }
      //Status
      case h :: t if h.name == EffectType.status => {
        val effectParams = jsonEffect.find(effect => effect.name == EffectType.status).get.params.head.asInstanceOf[StatusParams]
        returnedAttack = returnedEffect(new DoesDmgAndApplyStatus(basicDmgToDo, basicEnemyToAtk), effectParams)
        if (t.nonEmpty)
          resolveAttack(t)
      }
      case _ :: t => resolveAttack(t)
    }

    resolveAttack(jsonEffect)
    returnedAttack
  }

  private def returnedEffect(item: DoesNDmg, param:Params): AttackEffect = {
    totalParamsSeq = totalParamsSeq :+ param
    item.params = totalParamsSeq
    item
  }
}