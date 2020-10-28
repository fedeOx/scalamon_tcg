package common

import Events.Event

trait Observable {
  protected var observers: Seq[Observer] = Vector()

  def addObserver(observer: Observer): Unit = observers = observers :+ observer

  def notifyObservers(event: Event): Unit = observers.foreach(o => o.update(event))
}
