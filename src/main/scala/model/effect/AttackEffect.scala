package model.effect

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.game.Board
import model.game.Cards.PokemonCard
import model.effect.EffectType.effectType

trait AttackEffect {
  def useEffect(attackingBoard: Board, defendingBoard: Board): Unit
  var params: Seq[params]
  var totalDmgToEnemyPkm: Int
}

trait Effect {
  def name: effectType
  def params: Seq[params]
}

object Effect {
  implicit val decoder: Decoder[Effect] = new Decoder[Effect] {
    override def apply(c: HCursor): Result[Effect] = {
      for {
        _name <- c.downField("name").as[Option[String]]
       _params <- c.downField("name").as[String] match {
        case _ if _name.get == "ndmg" =>  c.downField("params").as[Seq[nDmgParams]]
        case _ if _name.get == "EachDmg"=> c.downField("params").as[Seq[eachDmgParams]]
        case _ if _name.get == "status"=> c.downField("params").as[Seq[statusParams]]
        case _ if _name.get == "EachEnergy"=> c.downField("params").as[Seq[eachEnergyParams]]
        case _ if _name.get == "DiscardEnergy"=> c.downField("params").as[Seq[discardEnergyParams]]
        case _ if _name.get == "DoesNDmgAndDmgMyself"=> c.downField("params").as[Seq[dmgMyselfParams]]
        case _ if _name.get == "DoesNDmgAndDmgMyself_OR_doesNdmg"=> c.downField("params").as[Seq[dmgMyselfOrNotParams]]
        case _ if _name.get == "doesNDmgAndSetImmunity"=> c.downField("params").as[Seq[setImmunityParams]]
        case _ if _name.get == "recovery"=> c.downField("params").as[Seq[recoveryParams]]

        case _ => null
      }
      } yield {
        new Effect {
          override def name: effectType = EffectType.withName(_name.get)
          override def params: Seq[params] = _params
        }
      }
    }

  }
}

