package model.core

import common.{Observable, TurnOwner}
import common.TurnOwner.TurnOwner
import model.event.Events.Event
import model.exception.CoinNotLaunchedException

import scala.util.Random

object TurnManager extends Observable {
  private var turnOwner: Option[TurnOwner] = None
  private var acks: Int = 0
  private val TotalNumberOfAckRequired = 2

  def flipACoin(): Unit = {
    val side = new Random().nextInt(2)
    if (side == 0)
      turnOwner = Some(TurnOwner.Opponent)
    else
      turnOwner = Some(TurnOwner.Player)
    this.notifyObservers(Event.flipCoinEvent(turnOwner.get.equals(TurnOwner.Player)))
  }

  @throws(classOf[CoinNotLaunchedException])
  def playerReady(): Unit = {
    if (turnOwner.nonEmpty) {
      this.synchronized {
        acks = acks + 1
        if (acks == TotalNumberOfAckRequired) {
          this.notifyObservers(Event.nextTurnEvent(turnOwner.get))
        }
      }
    } else {
      throw new CoinNotLaunchedException("It is required to flip a coin before")
    }
  }

  @throws(classOf[CoinNotLaunchedException])
  def switchTurn(): Unit = {
    if (turnOwner.nonEmpty) {
      if (turnOwner.get == TurnOwner.Player) {
        turnOwner = Some(TurnOwner.Opponent)
      } else {
        turnOwner = Some(TurnOwner.Player)
      }
      this.notifyObservers(Event.nextTurnEvent(turnOwner.get))
    } else {
      throw new CoinNotLaunchedException("It is required to flip a coin before")
    }
  }

  override def reset(): Unit = {
    super.reset()
    acks = 0
  }

}
