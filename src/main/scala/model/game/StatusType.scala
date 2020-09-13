package model.game

object StatusType extends Enumeration {
  type StatusType = Value
  val NoStatus : Value = Value("noStatus")
  val Paralyzed: Value = Value("paralyzed")
  val Confused: Value = Value("confused")
  val Asleep: Value = Value("asleep")
  val Poisoned: Value = Value("poisoned")

  def withNameWithDefault(name: String): Value =
    values.find(_.toString.toLowerCase == name.toLowerCase()).getOrElse(NoStatus)
}
