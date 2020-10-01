package model.core

import common.{Observable, TurnOwner}
import common.TurnOwner.TurnOwner
import model.event.Events.Event
import model.exception.CoinNotLaunchedException

import scala.util.Random

trait TurnManager extends Observable {
  def flipACoin(): Unit

  @throws(classOf[CoinNotLaunchedException])
  def playerReady(): Unit

  @throws(classOf[CoinNotLaunchedException])
  def switchTurn(): Unit

  def reset(): Unit
}

object TurnManager {
  def apply(): TurnManager = TurnManagerImpl()

  private case class TurnManagerImpl() extends TurnManager {
    private var turnOwner: Option[TurnOwner] = None
    private var acks: Int = 0
    private val TotalNumberOfAckRequired = 2

    import common.CoinUtil
    import common.CoinUtil.CoinValue

    def flipACoin(): Unit = {
      turnOwner = CoinUtil.flipACoin() match {
        case CoinValue.Tail => Some(TurnOwner.Opponent)
        case CoinValue.Head => Some(TurnOwner.Player)
      }
      this.notifyObservers(Event.flipCoinEvent(turnOwner.get.equals(TurnOwner.Player)))
    }

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

}
