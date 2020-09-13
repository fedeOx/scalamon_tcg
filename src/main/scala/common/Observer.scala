package common

import model.event.Events.Event

trait Observer {
  def update(event: Event): Unit
}
