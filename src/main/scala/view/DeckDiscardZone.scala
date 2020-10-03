package view

import model.game.Cards.Card
import scalafx.geometry.Pos
import scalafx.scene.layout.VBox
import scalafx.scene.shape.Box
import view.CardCreator.createCard

/**
 * The field Zone that contains the deck and the discard stack
 */
trait DeckDiscardZone extends VBox {
  /**
   * Updates the VBox children
   * @param card: the card on top of the discard stack
   */
  def updateView(card: Card): Unit
}

object DeckDiscardZone {
  /**
   * Creates an instance of DeckDiscardZone
   * @return an instance of DeckDiscardZone
   */
  def apply(): DeckDiscardZone = DeckDiscardZoneImpl()

  private case class DeckDiscardZoneImpl() extends DeckDiscardZone {
    private val WIDTH = 10
    private val HEIGHT = 25
    private val deck = createCard("/assets/cardBack.jpg",cardType = CardType.Deck)
    private var discardStack : Box = _
    children = deck

    alignment = Pos.Center
    prefWidth = WIDTH
    prefHeight = HEIGHT
    styleClass += "deckDiscard"
    translateX = 45

    //TODO: pattern strategy
    def updateView(card: Card) : Unit = {
      discardStack = createCard("/assets/"+card.belongingSetCode+"/"+card.imageId+".jpg", cardType = CardType.DiscardStack)
      children = List(deck, discardStack)
    }
  }
}