package model.game

import io.circe.Decoder
import model.game.EnergyType.EnergyType

trait Resistance {
  val energyType: EnergyType
  val reduction: Int
}
object Resistance {
  def apply(energyType: EnergyType, reduction: Int): Resistance = ResistanceImpl(energyType, reduction.abs)

  implicit val decoder: Decoder[Resistance] = Decoder.forProduct2("type", "value")(Resistance.apply)

  private case class ResistanceImpl(override val energyType: EnergyType,
                                    override val reduction: Int) extends Resistance
}
