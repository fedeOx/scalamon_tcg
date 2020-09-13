package model.core

import java.util.Observable

import common.TurnOwner.TurnOwner
import model.core.TurnManager.TurnManagerStatus.TurnManagerStatus

object TurnManager extends Observable {
  object TurnManagerStatus extends Enumeration {
    type TurnManagerStatus = Value
    val player: Value = Value("player")
    val opponent: Value = Value("opponent")
  }

  private var status: TurnManagerStatus = TurnManagerStatus.player
  private var turnOwner: TurnOwner = _

  def flipACoin(): TurnOwner = ???

  def playerReady(): Unit = ???

  def aiReady(): Unit = ???

  def switchTurn(): Unit = ???

}
