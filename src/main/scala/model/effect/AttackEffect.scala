package model.effect

import controller.Controller
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.core.GameManager
import model.game.Board
import model.game.Cards.PokemonCard
import model.effect.EffectType.effectType

trait AttackEffect {
  def useEffect(attackingBoard: Board, defendingBoard: Board): Unit
  var params: Seq[Params]
  var totalDmgToEnemyPkm: Int
}

trait Effect {
  def name: effectType
  def params: Seq[Params]
}

object Effect {
  implicit val decoder: Decoder[Effect] = new Decoder[Effect] {
    override def apply(c: HCursor): Result[Effect] = {
      for {
        _name <- c.downField("name").as[Option[String]]
       _params <- c.downField("name").as[String] match {
        case _ if _name.get == "ndmg" =>  c.downField("params").as[Seq[NDmgParams]]
        case _ if _name.get == "EachDmg"=> c.downField("params").as[Seq[EachDmgParams]]
        case _ if _name.get == "status"=> c.downField("params").as[Seq[StatusParams]]
        case _ if _name.get == "EachEnergy"=> c.downField("params").as[Seq[EachEnergyParams]]
        case _ if _name.get == "DiscardEnergy"=> c.downField("params").as[Seq[DiscardEnergyParams]]
        case _ if _name.get == "DoesNDmgAndDmgMyself"=> c.downField("params").as[Seq[DmgMyselfParams]]
        case _ if _name.get == "DoesNDmgAndDmgMyself_OR_doesNdmg"=> c.downField("params").as[Seq[DmgMyselfOrNotParams]]
        case _ if _name.get == "doesNDmgAndSetImmunity"=> c.downField("params").as[Seq[SetImmunityParams]]
        case _ if _name.get == "recovery"=> c.downField("params").as[Seq[RecoveryParams]]
        case _ if _name.get == "toBench"=> c.downField("params").as[Seq[ToBenchParams]]
        case _ if _name.get == "prevent"=> c.downField("params").as[Seq[PreventParams]]
        case _ => null
      }
      } yield {
        new Effect {
          override def name: effectType = EffectType.withName(_name.get)
          override def params: Seq[Params] = _params
        }
      }
    }

  }
}

