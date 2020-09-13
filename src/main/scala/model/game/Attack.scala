package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.game.EnergyType.EnergyType

import scala.util.Try

trait Attack {
  def name: String
  def cost: Seq[EnergyType]
  def damage: Option[Int]
  // def effect: Effect // TODO
}
object Attack {
  implicit val decoder: Decoder[Attack] = new Decoder[Attack] {
    override def apply(c: HCursor): Result[Attack] =
      for {
        _name <- c.downField("name").as[String]
        _costs <- c.downField("cost").as[Seq[EnergyType]]
        _damage <- c.downField("damage").as[String]
      } yield {
        new Attack {
          override def name: String = _name
          override def cost: Seq[EnergyType] = _costs
          override def damage: Option[Int] = Try(_damage.replaceAll("[^0-9.]", "").toInt).toOption
        }
      }
  }
}
