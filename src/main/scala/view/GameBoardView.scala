package view

import common.TurnOwner.TurnOwner
import common.{Observer, TurnOwner}
import controller.Controller
import javafx.scene.paint.ImagePattern
import model.core.{GameManager, TurnManager}
import model.event.Events
import model.event.Events.Event._
import model.ia.Ia
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.image.Image
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{Group, PerspectiveCamera, Scene, SceneAntialiasing}
import scalafx.stage.{Modality, Stage, Window}

/** *
 * Stage that contains the game scene
 */
class GameBoardView extends JFXApp.PrimaryStage with Observer {
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"
  private val parentWindow : Window = this
  val zoomZone = new ZoomZone
  private val opponentBoard = new PlayerBoard(false, zoomZone, parentWindow)
  private val humanBoard = new PlayerBoard(true, zoomZone, parentWindow)
  private var turnOwner : TurnOwner = TurnOwner.Player
  private var loadingMessage : Stage = _
  title = TITLE
  icons += new Image("/assets/icon.png")
  Ia.start()
  GameManager.addObserver(this)
  TurnManager.addObserver(this)
  x = 0
  y = 0
  scene = new Scene(WIDTH, HEIGHT, true, SceneAntialiasing.Balanced) {
    stylesheets = List("/style/PlayerBoardStyle.css")
    camera = new PerspectiveCamera(true) {
      transforms += (
        new Rotate(50, Rotate.XAxis),
        new Translate(0, 5, -75))
    }
    val playMatMaterial = new PhongMaterial()
    playMatMaterial.diffuseMap = new Image("/assets/playmat.png")
    fill = new ImagePattern(new Image("/assets/woodTable.png"))

    content = List(new Box {
      width = 55
      height = 50
      depth = 0.4
      material = playMatMaterial
    }, new Group {
      translateX = -27.5
      translateY = -25
      minWidth(55)
      minHeight(50)
      alignmentInParent = Pos.Center
      children = Seq(opponentBoard, humanBoard)
    }, zoomZone)

    loadingMessage = PopupBuilder.openLoadingScreen(parentWindow)
    loadingMessage.show()
  }

  resizable = true
  sizeToScene()
  show()

  onCloseRequest = _ => Ia.interrupt()

  override def update(event: Events.Event): Unit = event match {
    case event if  event.isInstanceOf[BuildGameField] => {
      humanBoard.board = event.asInstanceOf[BuildGameField].playerBoard
      opponentBoard.board = event.asInstanceOf[BuildGameField].opponentBoard
      Platform.runLater(humanBoard.updateHand())
      Platform.runLater(humanBoard.updateActive())
      Platform.runLater(PopupBuilder.closeLoadingScreen(loadingMessage))
    }
    case event if event.isInstanceOf[FlipCoin] =>{
      turnOwner = event.asInstanceOf[FlipCoin].coinValue
    }
    case event : UpdatePlayerBoard => {
      Platform.runLater({
        humanBoard.updateActive()
        humanBoard.updateHand()
        humanBoard.updateBench()
        humanBoard.updateDiscardStack()
      })
    }
    case event : UpdateOpponentBoard => {
      Platform.runLater({
        opponentBoard.updateBench()
        opponentBoard.updateActive()
        opponentBoard.updateDiscardStack()
      })
    }
    case event : NextTurn => {
      humanBoard.disable = !(event.turnOwner == TurnOwner.Player)
      if(event.turnOwner == TurnOwner.Player)
        Platform.runLater({
          openTurnScreen(this)
          utils.controller.drawACard()
          humanBoard.updateHand()
          //opponentBoard.updateBench()
          //opponentBoard.updateActive()
        })
      Platform.runLater({
        opponentBoard.updateBench()
        opponentBoard.updateActive()
        opponentBoard.updateDiscardStack()
      })
    }
    case event : PokemonKO => {
      println("pokemonKO event")
      if (humanBoard.board.activePokemon.get.isKO)
        Platform.runLater(PopupBuilder.openBenchSelectionScreen(this,humanBoard.board.pokemonBench))
      else {
        utils.controller.drawAPrizeCard()
      }
    }
    case _ =>
  }

  def openTurnScreen(parent: Window) : Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = new Label("È il tuo turno")
      }
      sizeToScene()
      //x = parentWindow.getX + parentWindow.getWidth / 1.6
      //y = parentWindow.getY + parentWindow.getHeight / 2.8
      resizable = false
      alwaysOnTop = true
    }
    dialog.show()
    Thread.sleep(1000)
    dialog.close()
  }

}

//TODO: orribile
object utils {
  var controller = Controller()
}
