package model.core

import common.{Observable, TurnOwner}
import common.TurnOwner.TurnOwner
import model.event.Events.Event

import scala.util.Random

object TurnManager extends Observable {
  private var turnOwner: TurnOwner = _
  private var acks: Int = 0
  private val TotalNumberOfAckRequired = 2

  def flipACoin(): TurnOwner = {
    val side = new Random().nextInt(2)
    if (side == 0)
      turnOwner = TurnOwner.Opponent
    else
      turnOwner = TurnOwner.Player
    turnOwner
  }

  def playerReady(): Unit = {
    this.synchronized {
      acks = acks + 1
      if (acks == TotalNumberOfAckRequired) {
        this.notifyObservers(Event.nextTurnEvent(turnOwner))
      }
    }
  }

  def switchTurn(): Unit = {
    if (turnOwner == TurnOwner.Player) {
      turnOwner = TurnOwner.Opponent
    } else {
      turnOwner = TurnOwner.Player
    }
    this.notifyObservers(Event.nextTurnEvent(turnOwner))
  }

}
