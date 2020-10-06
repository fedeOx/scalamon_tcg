package view.game

import model.game.Cards.Card
import scalafx.geometry.Pos
import scalafx.scene.layout.VBox
import scalafx.scene.shape.Box
import view.game.CardCreator._

/**
 * The field Zone that contains the deck and the discard stack
 */
trait DeckDiscardZone extends VBox {
  /**
   * Updates the VBox children
   * @param deck: the board's deck
   * @param discardStack: the board's discard stack
   */
  def updateView(deck: Seq[Card], discardStack: Seq[Card]): Unit
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
    private val deckBox = createCard("/assets/cardBack.jpg",cardType = CardType.Deck)
    private var discardStackBox : Box = _
    children = deckBox

    alignment = Pos.Center
    prefWidth = WIDTH
    prefHeight = HEIGHT
    styleClass += "deckDiscard"
    translateX = 45

    def updateView(deck: Seq[Card], discardStack: Seq[Card]) : Unit = {
      var list = Seq[Box]()
      if (deck.nonEmpty)
        list = list :+ deckBox
      if(discardStack.nonEmpty) {
        discardStackBox = createCard("/assets/"+discardStack.last.belongingSetCode+"/"+
          discardStack.last.imageNumber+".png", cardType = CardType.DiscardStack)
        list = list :+ discardStackBox
      }
      children = list
    }
  }
}