package model

import io.circe.{Decoder, HCursor}
import io.circe.Decoder.Result
import model.EnergyType.EnergyType

import scala.util.Try

trait Attack {
  def name: String
  def costMap: Map[EnergyType, Int]
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
        val map = _costs.foldLeft(scala.collection.mutable.Map[EnergyType, Int]().withDefaultValue(0)){
          (map, energy) => map(energy) += 1; map
        }.toMap
        new Attack {
          override def name: String = _name
          override def costMap: Map[EnergyType, Int] = map
          override def damage: Option[Int] = Try(_damage.replaceAll("[^0-9.]", "").toInt).toOption
        }
      }
  }
}
