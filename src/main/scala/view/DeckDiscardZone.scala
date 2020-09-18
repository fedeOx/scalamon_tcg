package view

import model.game.Cards.Card
import scalafx.geometry.Pos
import scalafx.scene.layout.VBox
import scalafx.scene.shape.Box
import view.CardCreator.createCard

import scala.collection.mutable

/***
 * The Zone for the deck and the discard stack
 */
case class DeckDiscardZone() extends VBox {
  private val WIDTH = 10
  private val HEIGHT = 25
  //private val discardStack = mutable.Stack("/assets/1.jpg", "/assets/4.jpg")
  private var deck = createCard("/assets/cardBack.jpg",cardType = CardType.Deck)
  private var discardStack : Box = _
  children = deck

  alignment = Pos.Center
  prefWidth = WIDTH
  prefHeight = HEIGHT
  styleClass += "deckDiscard"
  translateX = 45

  def updateView(card: Card) : Unit = {
    discardStack = createCard("/assets/base1/"+card.imageId+".jpg", cardType = CardType.DiscardStack)
    children = List(deck, discardStack)
  }
}
