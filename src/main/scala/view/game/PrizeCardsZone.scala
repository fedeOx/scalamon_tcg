package view.game

import model.card.Card
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout.VBox
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box

/**
 * The field zone for the prize cards
 */
trait PrizeCardsZone extends VBox {
  /**
   * Updates the Vbox children
   *
   * @param prizes : the prize cards
   */
  def updateView(prizes: Seq[Card]): Unit
}

object PrizeCardsZone {
  /**
   * Creates an instance of PrizesCardZone
   *
   * @param isHumans : true if it's the human's PrizeCardsZone
   * @param board    : the parent PlayerBoard
   * @return an instance of PrizesCardZone
   */
  def apply(isHumans: Boolean, board: PlayerBoard): PrizeCardsZone = PrizeCardsZoneImpl(isHumans, board)

  private case class PrizeCardsZoneImpl(isHumans: Boolean, board: PlayerBoard) extends PrizeCardsZone {
    private val WIDTH = 10
    private val HEIGHT = 25

    alignment = Pos.Center
    prefWidth = WIDTH
    prefHeight = HEIGHT
    styleClass += "prizeCards"

    def updateView(prizes: Seq[Card]): Unit = {
      children = List(CardFactory(cardType = CardType.Prize, "/assets/cardBack.jpg"), new Box {
        val numberMaterial = new PhongMaterial()
        numberMaterial.diffuseMap = new Image("/assets/prize" + prizes.size + ".png")
        material = numberMaterial
        width = 3
        height = 4
        depth = 0.5
        if (!isHumans)
          rotate = 180
      })
    }
  }

}

