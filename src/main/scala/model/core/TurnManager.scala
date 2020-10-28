package model.core

import common.{Observable, TurnOwner}
import common.TurnOwner.TurnOwner
import common.Events.NextTurnEvent
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
    private val NumberOfAckRequired = 1

    import common.CoinUtil
    import common.CoinUtil.CoinValue

    def flipACoin(): Unit = turnOwner = CoinUtil.flipACoin() match {
      case CoinValue.Tail => Some(TurnOwner.Opponent)
      case CoinValue.Head => Some(TurnOwner.Player)
    }

    def playerReady(): Unit = this.synchronized {
      turnOwner match {
        case Some(owner) if acks == NumberOfAckRequired => this.notifyObservers(NextTurnEvent(owner))
        case Some(_) => acks = acks + 1
        case _ => throw new CoinNotLaunchedException("It is required to flip a coin before")
      }
    }

    def switchTurn(): Unit = {
      def _switchOwner(): TurnOwner = turnOwner match {
        case Some(owner) if owner == TurnOwner.Player => turnOwner = Some(TurnOwner.Opponent); turnOwner.get
        case Some(owner) if owner == TurnOwner.Opponent => turnOwner = Some(TurnOwner.Player); turnOwner.get
        case _ => throw new CoinNotLaunchedException("It is required to flip a coin before")
      }
      this.notifyObservers(NextTurnEvent(_switchOwner()))
    }
  }
}
