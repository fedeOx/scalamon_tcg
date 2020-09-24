package view

import javafx.geometry.Insets
import model.game.Cards.{Card, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.StatusType.StatusType
import model.game.{Board, StatusType}
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Group
import scalafx.scene.image.Image
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate
import scalafx.stage.Window

/***
 * Player board component representing one size of the game board
 * isHumans: true if it's the board of the human player
 * zoom: the zone in which the zoomed cards are generated
 *
 * @param isHumans : true if it's the human's board
 */
class PlayerBoard(isHumans: Boolean, parentWindow: Window) extends Group {
  private val WIDTH = 55
  private val HEIGHT = 25
  val gameWindow : Window = parentWindow
  private val prize = PrizeCardsZone()
  private val active = ActivePkmnZone(isHumans, this)
  private val bench = BenchZone(isHumans, this)
  private val deckDiscard = DeckDiscardZone()
  private var hand : HandZone = _
  var myBoard : Board = _
  var opponentBoard : Board = _
  var isFirstTurn : Boolean = true

  styleClass += "humanPB"
  children = List(prize, active, bench, deckDiscard)
  if (isHumans) {
    hand = HandZone(isHumans, this)
    children += hand
    val endTurnButton: Box = new Box{
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
          gameWindow.asInstanceOf[GameBoardView].controller.playerReady()
        } else
          gameWindow.asInstanceOf[GameBoardView].controller.endTurn()
        println("fine turno")
      }
    }
    children += endTurnButton
  }

  minWidth(WIDTH)
  minHeight(HEIGHT)
  maxWidth(WIDTH)
  maxHeight(HEIGHT)

  if (!isHumans) rotate = 180 else translateY = 25

  def updateHand() : Unit = hand.updateView(myBoard.hand)
  def updateActive() : Unit = active.updateView(myBoard.activePokemon)
  def updateBench() : Unit = bench.updateView(myBoard.pokemonBench)
  def updateDiscardStack() : Unit = if (myBoard.discardStack.nonEmpty) deckDiscard.updateView(myBoard.discardStack.last)
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
    cardMaterial.diffuseMap = new Image(new Image("/assets/"+card.belongingSetCode+"/"+card.imageId+".jpg"))
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


