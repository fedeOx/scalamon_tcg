package view

import scalafx.Includes._
import scalafx.scene.Group
import scalafx.scene.layout.HBox
import scalafx.stage.Window

/***
 * Player board component for the player
 * isHumans: true if it's the board of the human player
 * zoom: the zone in which the zoomed cards are generated
 *
 * @param isHumans : true if it's the human's board
 * @param zoom : the zone for the zoomed cards
 */
class PlayerBoard(isHumans: Boolean, zoom: ZoomZone, parentWindow: Window) extends Group {
  private val WIDTH = 55
  private val HEIGHT = 25
  styleClass += "humanPB"
  children = List(new PrizeCardsZone, new ActivePkmnZone(zoom, isHumans, this, parentWindow), new BenchZone(zoom, isHumans),new DeckDiscardZone)
  if (isHumans)
    children += new HandZone(zoom, isHumans)

  minWidth(WIDTH)
  minHeight(HEIGHT)

  maxWidth(WIDTH)
  maxHeight(HEIGHT)

  if (!isHumans) rotate = 180 else translateY = 25
}

class ZoomZone extends HBox {
  maxWidth = 13
  maxHeight = 18.2
  translateX = -27
  translateY = 0
  translateZ = -9
}


