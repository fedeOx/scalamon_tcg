package model

object StatusType extends Enumeration {
  type statusType = Value
  val noStatus : Value = Value("noStatus")
  val paralyzed: Value = Value("paralyzed")
  val confused: Value = Value("confused")
  val asleep: Value = Value("asleep")
  val poisoned: Value = Value("poisoned")

  def withNameWithDefault(name: String): Value =
    values.find(_.toString.toLowerCase == name.toLowerCase()).getOrElse(noStatus)
}
