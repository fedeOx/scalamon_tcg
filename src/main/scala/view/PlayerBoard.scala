package view

import common.Observer
import controller.Controller
import model.core.{DataLoader, GameManager}
import model.event.Events
import model.event.Events.Event
import model.event.Events.Event.BuildGameField
import model.game.{Board, DeckCard, DeckType, SetType}
import model.game.Cards.{Card, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.StatusType.StatusType
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.Group
import scalafx.scene.image.Image
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate
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
  def updateActive() : Unit = active.updateView(board.activePokemon)
}

class ZoomZone extends HBox {
  maxWidth = 13
  maxHeight = 18.2
  translateX = -27
  translateY = 0
  translateZ = -9
  alignment = Pos.TopLeft

  def showContent(card: Card): Unit = {
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(new Image("/assets/base1/"+card.imageId+".jpg"))
    children = Seq(new Box {
      depth = 0.1
      width = 13
      height = 18.2
      material = cardMaterial
      translateY = -3
      transforms += new Rotate(50, Rotate.XAxis)
    })
    card match {
      case pokemonCard: PokemonCard => children += visualizePokemonInfo(pokemonCard)
      case _ =>
    }
  }

  private def visualizePokemonInfo(card: PokemonCard): VBox = {
    val infoBox : VBox = new VBox()
    infoBox.alignment = Pos.TopLeft
    infoBox.transforms += new Rotate(50, Rotate.XAxis)
    if (card.initialHp != card.actualHp)
      infoBox.children += createInfoBox(card.initialHp - card.actualHp)
    infoBox
  }


  private def createInfoBox(args: Any): Box = args match {
    case damage : Int => generateBox(new Image("/assets/dmg/"+ damage +".png"))
    case status if args.isInstanceOf[StatusType] => ???
    case energy if args.isInstanceOf[EnergyType] => ???
  }

  private def generateBox(image: Image) : Box = {
    new Box {
      depth = 0.1
      width = 2
      height = 2
      val playMatMaterial = new PhongMaterial()
      playMatMaterial.diffuseMap = image
      material = playMatMaterial
    }
  }
}


