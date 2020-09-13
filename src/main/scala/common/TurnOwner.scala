package common

object TurnOwner extends Enumeration {
  type TurnOwner = Value
  val Player: Value = Value("player")
  val Opponent: Value = Value("opponent")
}
