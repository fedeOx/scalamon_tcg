package view

import javafx.geometry.Insets
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.scene.shape.Box
import scala.collection.mutable
import scala.collection.mutable._
import CardCreator._

/** *
 * Player board component for the player
 * isHumans: true if it's the board of the human player
 * zoom: the zone in which the zoomed cards are generated
 *
 * @param isHumans : true if it's the human's board
 * @param zoom : the zone for the zoomed cards
 */
class PlayerBoard(isHumans: Boolean, zoom: ZoomZone) extends GridPane {
  styleClass += "humanPB"

  add(new PrizeCardsZone, 0, 0, 1, 2)
  add(new ActivePkmnZone(zoom, isHumans), 1, 0, 1, 1)
  add(new BenchZone(zoom, isHumans), 1, 1, 1, 1)
  add(new DeckDiscardZone, 2, 0, 1, 2)
  if (isHumans)
    add(new HandZone(zoom,isHumans),0,3,3,1)

  minWidth = 55
  minHeight = 25

  maxWidth = 55
  maxHeight = 25

  if (!isHumans) rotate = 180
}

class PrizeCardsZone extends VBox {
  children = List(createCard("/assets/cardBack.jpg",cardType = CardType.prize))
  alignment = Pos.Center
  minHeight = 25
  maxHeight = 25
  minWidth = 10
  maxWidth = 10
  styleClass += "prizeCards"
}

class DeckDiscardZone extends VBox {
  private val discardStack = mutable.Stack("/assets/1.jpg", "/assets/4.jpg")
  children = List(createCard("/assets/cardBack.jpg",cardType = CardType.deck),
    createCard(discardStack.top, cardType = CardType.discardStack))

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
  children = createCard("/assets/4.jpg", Some(zone), cardType = CardType.active, isHumans = Some(isHumans))
}

class BenchZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  private var bench : mutable.Seq[Box] = mutable.Seq()

  for (cardIndex <- 0 to 4) {
    bench = bench :+ createCard("/assets/1.jpg", Some(zone), CardType.bench, cardIndex = cardIndex, isHumans = Some(isHumans))
  }

  children = bench
  spacing = 0.5
  minWidth = 35
  maxWidth = 35
  minHeight = 10
  maxHeight = 10
  alignment = Pos.BottomLeft

}

class HandZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  private var hand : mutable.Seq[Box] = mutable.Seq()
  for (cardIndex <- 0 to 7) {
     hand = hand :+ createCard("/assets/4.jpg", Some(zone), CardType.hand, 0.6*cardIndex,
       cardIndex = cardIndex, isHumans = Some(isHumans))
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


