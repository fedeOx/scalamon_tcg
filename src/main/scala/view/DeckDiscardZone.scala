package view

import scalafx.geometry.Pos
import scalafx.scene.layout.VBox
import view.CardCreator.createCard

import scala.collection.mutable

/***
 * The Zone for the deck and the discard stack
 */
case class DeckDiscardZone() extends VBox {
  private val WIDTH = 10
  private val HEIGHT = 25
  private val discardStack = mutable.Stack("/assets/1.jpg", "/assets/4.jpg")
  children = List(createCard("/assets/cardBack.jpg",cardType = CardType.Deck),
    createCard(discardStack.top, cardType = CardType.DiscardStack))

  alignment = Pos.Center
  /*minWidth = 10
  maxWidth = 10
  minHeight = 25
  maxHeight = 25*/
  prefWidth = WIDTH
  prefHeight = HEIGHT
  styleClass += "deckDiscard"
  translateX = 45
}
