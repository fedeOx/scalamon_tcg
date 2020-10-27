package common

import Events.Event

trait Observer {
  def update(event: Event): Unit
}
