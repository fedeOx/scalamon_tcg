package model.game

import io.circe.Decoder
import model.game.EnergyType.EnergyType
import model.game.Weakness.Operation.Operation

trait Weakness {
  val energyType: EnergyType
  val operation: Operation
}
object Weakness {
  object Operation extends Enumeration {
    type Operation = Value
    val multiply2: Value = Value("×2")
  }

  def apply(energyType: EnergyType, value: String): Weakness = WeaknessImpl(energyType, Operation.withName(value))

  implicit val decoder: Decoder[Weakness] = Decoder.forProduct2("type", "value")(Weakness.apply)

  private case class WeaknessImpl(override val energyType: EnergyType,
                                  override val operation: Operation) extends Weakness
}
