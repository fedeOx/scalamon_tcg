package view

import scalafx.geometry.Pos
import scalafx.scene.layout.VBox
import view.CardCreator.createCard

/***
 * The Zone for the prize cards
 */
case class PrizeCardsZone() extends VBox {
  private val WIDTH = 10
  private val HEIGHT = 25

  children = List(createCard("/assets/cardBack.jpg",cardType = CardType.prize))
  alignment = Pos.Center
  prefWidth = WIDTH
  prefHeight = HEIGHT
  styleClass += "prizeCards"

}
