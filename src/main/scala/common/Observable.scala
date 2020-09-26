package common

import model.event.Events.Event

trait Observable {
  private var observers: Seq[Observer] = Vector()

  def addObserver(observer: Observer): Unit = observers = observers :+ observer

  def notifyObservers(event: Event): Unit = observers.foreach(o => o.update(event))

  def reset(): Unit = observers = Vector()
}
