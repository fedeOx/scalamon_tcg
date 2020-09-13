package model.core

import common.{Observable, TurnOwner}
import common.TurnOwner.TurnOwner
import model.core.TurnManager.TurnManagerStatus.TurnManagerStatus

import scala.util.Random

object TurnManager extends Observable {
  object TurnManagerStatus extends Enumeration {
    type TurnManagerStatus = Value
    val player: Value = Value("player")
    val opponent: Value = Value("opponent")
  }

  private var status: TurnManagerStatus = TurnManagerStatus.player
  private var turnOwner: TurnOwner = _

  def flipACoin(): TurnOwner = {
    val side = new Random().nextInt(2)
    if (side == 0)
      turnOwner = TurnOwner.Opponent
    else
      turnOwner = TurnOwner.Player
    turnOwner
  }

  def playerReady(): Unit = ???

  def aiReady(): Unit = ???

  def switchTurn(): Unit = ???

}
