package model.core

import common.{Observable, TurnOwner}
import common.TurnOwner.TurnOwner
import model.event.Events.NextTurnEvent
import model.exception.CoinNotLaunchedException

trait TurnManager extends Observable {
  /**
   * Flips the initial game coin.
   */
  def flipACoin(): Unit

  /**
   * To be called when a player has completed his initial turn.
   * @throws model.exception.CoinNotLaunchedException if the initial game coin is not been launched yet.
   */
  @throws(classOf[CoinNotLaunchedException])
  def playerReady(): Unit

  /**
   * To be called when a player completes his turn.
   * @throws model.exception.CoinNotLaunchedException if the initial game coin is not been launched yet.n
   */
  @throws(classOf[CoinNotLaunchedException])
  def switchTurn(): Unit
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
    }

    def playerReady(): Unit = {
      if (turnOwner.nonEmpty) {
        this.synchronized {
          acks = acks + 1
          if (acks == TotalNumberOfAckRequired) {
            this.notifyObservers(NextTurnEvent(turnOwner.get))
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
        this.notifyObservers(NextTurnEvent(turnOwner.get))
      } else {
        throw new CoinNotLaunchedException("It is required to flip a coin before")
      }
    }
  }

}
