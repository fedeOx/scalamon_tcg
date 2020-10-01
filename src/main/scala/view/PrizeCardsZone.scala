package view

import model.game.Cards.Card
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout.VBox
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate
import view.CardCreator.createCard

/***
 * The Zone for the prize cards
 */
case class PrizeCardsZone(isHumans: Boolean, board: PlayerBoardImpl) extends VBox {
  private val WIDTH = 10
  private val HEIGHT = 25

  alignment = Pos.Center
  prefWidth = WIDTH
  prefHeight = HEIGHT
  styleClass += "prizeCards"

  def updateView(prizes: Seq[Card]) : Unit = {
    children = List(createCard("/assets/cardBack.jpg",cardType = CardType.Prize), new Box {
      val numberMaterial = new PhongMaterial()
      numberMaterial.diffuseMap = new Image("/assets/prize"+prizes.size+".png")
      material = numberMaterial
      width = 3
      height = 4
      depth = 0.5
      if (!isHumans)
        rotate = 180
    })
  }

}
