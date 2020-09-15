package view

import common.Observer
import controller.Controller
import model.core.{DataLoader, GameManager}
import model.event.Events
import model.event.Events.Event
import model.event.Events.Event.BuildGameField
import model.game.{Board, DeckCard, DeckType, SetType}
import model.game.Cards.{Card, PokemonCard}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.Group
import scalafx.scene.layout.HBox
import scalafx.stage.Window

/***
 * Player board component for the player
 * isHumans: true if it's the board of the human player
 * zoom: the zone in which the zoomed cards are generated
 *
 * @param isHumans : true if it's the human's board
 * @param zoom : the zone for the zoomed cards
 */
class PlayerBoard(isHumans: Boolean, zoom: ZoomZone, parentWindow: Window) extends Group {
  private val WIDTH = 55
  private val HEIGHT = 25

  private var prize = PrizeCardsZone()
  private var active = ActivePkmnZone(zoom, isHumans, this, parentWindow)
  private var bench = BenchZone(zoom, isHumans)
  private var deckDiscard = DeckDiscardZone()
  private var hand : HandZone = _
  var board : Board = _
  styleClass += "humanPB"
  children = List(prize, active, bench, deckDiscard)
  if (isHumans) {
    hand = HandZone(zoom, isHumans, this)
    children += hand
  }

  minWidth(WIDTH)
  minHeight(HEIGHT)

  maxWidth(WIDTH)
  maxHeight(HEIGHT)

  if (!isHumans) rotate = 180 else translateY = 25

  def updateHand() : Unit = hand.updateView(board.hand)
}

class ZoomZone extends HBox {
  maxWidth = 13
  maxHeight = 18.2
  translateX = -27
  translateY = 0
  translateZ = -9
}


