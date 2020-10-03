package view

import javafx.geometry.Insets
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate
import view.CardCreator._
import model.game.Cards._

/**
 * The field zone that contains the hand cards
 */
trait HandZone extends HBox {
  /**
   * Updates the HBox children
   * @param cards: the cards in the player's hand
   */
  def updateView(cards: Seq[Card]): Unit
}

object HandZone {
  /**
   * Creates an instance of HandZone
   * @param isHumans: true if it's the human's board
   * @param board: the parent PlayerBoard
   * @return an instance of HandZone
   */
  def apply(isHumans: Boolean, board: PlayerBoard): HandZone = HandZoneImpl(isHumans, board)

  private case class HandZoneImpl(isHumans: Boolean, board: PlayerBoard) extends HandZone {
    private var hand : Seq[Box] = Seq()
    private val parentBoard = board

    def updateView(cards: Seq[Card]) : Unit = {
      hand = Seq()
      cards.zipWithIndex.foreach{case (card,cardIndex) => {
        hand = hand :+ createCard("/assets/"+card.belongingSetCode+"/"+card.imageId+".jpg",
          Some(board.gameWindow.asInstanceOf[GameBoardView].zoomZone), CardType.Hand, 1*cardIndex,
          cardIndex = cardIndex, isHumans = Some(isHumans), Some(this), Some(parentBoard.myBoard), gameWindow = Some(parentBoard.gameWindow))
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
    margin = new Insets(0, 0,0,5)
  }
}

