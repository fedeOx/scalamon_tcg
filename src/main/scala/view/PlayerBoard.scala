package view

import javafx.geometry.Insets
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.scene.shape.Box
import scala.collection.mutable
import scala.collection.mutable._

/** *
 * Player board component for the player
 * isHumans: true if it's the board of the human player
 * zoom: the zone in which the zoomed cards are generated
 */
class PlayerBoard(isHumans: Boolean, zoom: ZoomZone) extends GridPane {
  styleClass += "humanPB"

  val zoomZone = zoom
  add(new PrizeCardsZone, 0, 0, 1, 2)
  add(new ActivePkmnZone(zoomZone, isHumans), 1, 0, 1, 1)
  add(new BenchZone(zoomZone, isHumans), 1, 1, 1, 1)
  add(new DeckDiscardZone, 2, 0, 1, 2)
  if (isHumans)
    add(new HandZone(zoomZone),0,3,3,1)


  minWidth = 55
  minHeight = 25

  maxWidth = 55
  maxHeight = 25

  if (!isHumans)
    rotate = 180

}

class PrizeCardsZone extends VBox {
  children = List(new CardComponent("/assets/cardBack.jpg").card)
  alignment = Pos.Center
  minHeight = 25
  maxHeight = 25
  minWidth = 10
  maxWidth = 10
  styleClass += "prizeCards"
}

class DeckDiscardZone extends VBox {
  private val discardStack = mutable.Stack("/assets/1.jpg", "/assets/4.jpg")
  children = List(new CardComponent("/assets/cardBack.jpg").card,
    new CardComponent(discardStack.top).card)

  alignment = Pos.Center
  maxWidth = 10
  maxHeight = 25
  styleClass += "deckDiscard"
}

class ActivePkmnZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  maxWidth = 35
  maxHeight = 15
  styleClass += "active"

  alignment = Pos.BottomCenter
  children = new CardComponent("/assets/4.jpg", Some(zone), isActive = true).card
}

class BenchZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  private var bench : Seq[Box] = Seq()

  for (pippo <- 0 to 4) {
    bench = bench :+ new CardComponent("/assets/1.jpg", Some(zone), 0.6*pippo ).card
  }

  children = bench
  spacing = 0.5
  minWidth = 35
  maxWidth = 35
  minHeight = 10
  maxHeight = 10
  alignment = Pos.BottomLeft

}

class HandZone(zone: ZoomZone) extends HBox {
  private var hand : Seq[Box] = Seq()
  for (pippo <- 0 to 7) {
     hand = hand :+ new CardComponent("/assets/4.jpg", Some(zone), 0.6*pippo ).card
  }
  children = hand
  alignment = Pos.BottomCenter
  maxWidth = 60
  minHeight = 10
  maxHeight = 20
  margin = new Insets(0, 0,0,5)
}

class ZoomZone extends HBox {
  maxWidth = 13
  maxHeight = 18.2
  translateX = -27
  translateY = 0
  translateZ = -5
}


