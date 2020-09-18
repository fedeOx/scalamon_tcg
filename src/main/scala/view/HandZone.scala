package view

import javafx.geometry.Insets
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate
import view.CardCreator._
import model.game.Cards._

import scala.collection.mutable

/***
 * The zone for the player's hand
 * @param zone: the zone for the zoomed cards
 * @param isHumans: true if it's the human's board
 */
case class HandZone(zone: ZoomZone, isHumans: Boolean, board: PlayerBoard) extends HBox {
  private var hand : Seq[Box] = Seq()
  private val parentBoard = board

  def updateView(cards: Seq[Card]) : Unit = {
    hand = Seq()
    cards.zipWithIndex.foreach{case (card,cardIndex) => {
      hand = hand :+ createCard("/assets/base1/"+card.imageId+".jpg", Some(zone), CardType.Hand, 1*cardIndex, //4.5 for Group
        cardIndex = cardIndex, isHumans = Some(isHumans), Some(this), Some(parentBoard.board))
    }}
    children = hand
  }
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
