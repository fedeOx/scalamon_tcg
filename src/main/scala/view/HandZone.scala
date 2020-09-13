package view

import javafx.geometry.Insets
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate
import view.CardCreator._

import scala.collection.mutable

/***
 * The zone for the player's hand
 * @param zone: the zone for the zoomed cards
 * @param isHumans: true if it's the human's board
 */
case class HandZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  private var hand : mutable.Seq[Box] = mutable.Seq()

  def updateView() : Unit = {
    hand = mutable.Seq()
    for (cardIndex <- 0 to 5) {
      hand = hand :+ createCard("/assets/4.jpg", Some(zone), CardType.hand, 1*cardIndex, //4.5 for Group
        cardIndex = cardIndex, isHumans = Some(isHumans), Some(this))
    }
    children = hand
  }
  updateView()
  alignment = Pos.TopCenter
  minWidth = 60
  maxWidth = 60
  minHeight = 10
  maxHeight = 20
  translateY = 25
  translateZ = -1
  transforms += new Rotate(10, Rotate.XAxis)
  /*
  translateY = 27
  translateX = 48
  alignmentInParent = Pos.BottomCenter
  maxHeight(20)
  minHeight(10)
  minWidth(60)
  maxWidth(60)*/
  margin = new Insets(0, 0,0,5)
}
