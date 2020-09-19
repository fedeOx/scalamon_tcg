package view

import common.Observer
import controller.Controller
import javafx.geometry.Insets
import model.core.{DataLoader, GameManager, TurnManager}
import model.event.Events
import model.event.Events.Event
import model.event.Events.Event.BuildGameField
import model.game.{Board, DeckCard, DeckType, SetType, StatusType}
import model.game.Cards.{Card, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.StatusType.StatusType
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.{Group, Scene}
import scalafx.scene.image.Image
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate
import scalafx.stage.{Modality, Stage, Window}

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
  private var bench = BenchZone(zoom, isHumans, this)
  private var deckDiscard = DeckDiscardZone()
  private var hand : HandZone = _
  private var isFirstTurn : Boolean = true
  private var loadingMessage : Stage = _
  var board : Board = _
  styleClass += "humanPB"
  children = List(prize, active, bench, deckDiscard)
  if (isHumans) {
    hand = HandZone(zoom, isHumans, this)
    children += hand
    val tasto = new Box{
      val buttonMaterial = new PhongMaterial()
      buttonMaterial.diffuseColor = Color.Blue
      material = buttonMaterial
      width = 3
      height = 2
      translateX = 42
      translateY = 5

      onMouseClicked = _ => {
        if(isFirstTurn) {
          isFirstTurn = false
          //controller.playerReady
          utils.controller.playerReady()
          //TurnManager.playerReady()
        } else
        utils.controller.endTurn()
          //TurnManager.switchTurn()
        println("fine turno")
      }
    }
    children += tasto

    loadingMessage = openLoadingScreen(parentWindow)
    loadingMessage.show()
  }

  minWidth(WIDTH)
  minHeight(HEIGHT)
  maxWidth(WIDTH)
  maxHeight(HEIGHT)

  if (!isHumans) rotate = 180 else translateY = 25


  def updateHand() : Unit = hand.updateView(board.hand)
  def updateActive() : Unit = active.updateView(board.activePokemon)
  def updateBench() : Unit = bench.updateView(board.pokemonBench)
  def updateDiscardStack() : Unit = if (board.discardStack.nonEmpty) deckDiscard.updateView(board.discardStack.last)

  //TODO: spostala in gameBoardView
  def openLoadingScreen(parent: Window) : Stage = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = new Label("Caricamento...")
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog
  }

  def closeLoadingScreen() : Unit = {
    loadingMessage.close()
  }
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
    infoBox.alignment = Pos.TopCenter
    infoBox.translateY = 5
    infoBox.translateX = 1.5
    infoBox.spacing = 0
    infoBox.translateZ = -9
    infoBox.transforms += new Rotate(50, Rotate.XAxis)
    if (card.initialHp != card.actualHp)
      infoBox.children += createInfoBox(card.initialHp - card.actualHp)
    if (!card.status.equals(StatusType.NoStatus))
      infoBox.children += createInfoBox(card.status)
    card.energiesMap.foreach(energy => {
      for (energies <- 0 until energy._2)
        infoBox.children += createInfoBox(energy._1)
    })
    infoBox
  }

  private def createInfoBox(args: Any): Box = args match {
    case damage : Int => generateBox(new Image("/assets/dmg/"+ damage +".png"))
    case status : StatusType => generateBox(new Image("/assets/status/"+status+".png"))
    case energy : EnergyType => generateBox(new Image("/assets/energy/"+energy+".png"))
  }

  private def generateBox(image: Image) : Box = {
    new Box {
      depth = 0.1
      width = 1.2
      height = 1.2
      val infoMaterial = new PhongMaterial()
      infoMaterial.diffuseMap = image
      material = infoMaterial
      margin = new Insets(0,0,-0.55,0)
    }
  }
}


