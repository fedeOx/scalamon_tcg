package common

import common.CoinUtil.CoinValue.CoinValue
import model.event.Events.{Event, FlipCoinEvent}

import scala.util.Random

object CoinUtil extends Observable {
  object CoinValue extends Enumeration {
    type CoinValue = Value
    val Head: Value = Value("head")
    val Tail: Value = Value("tail")
  }

  def flipACoin(): CoinValue = new Random().nextInt(2) match {
    case n if n == 0 => this.notifyObservers(FlipCoinEvent(false)); CoinValue.Tail
    case _ => this.notifyObservers(FlipCoinEvent(true)); CoinValue.Head
  }

  def reset(): Unit = {
    this.observers = Vector()
  }
}
