package view

import common.TurnOwner.TurnOwner
import common.{Observer, TurnOwner}
import controller.Controller
import javafx.scene.paint.ImagePattern
import model.core.{GameManager, TurnManager}
import model.event.Events
import model.event.Events.Event._
import model.game.Board
import model.ia.Ia
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Pos
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
  //private val parentWindow : Window = this
  val zoomZone = new ZoomZone
  val controller: Controller = Controller()
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"
  //private val guiEl: GuiElements = GuiElements(this, new ZoomZone)
  private val iABoard = new PlayerBoard(false,this)
  private val humanBoard = new PlayerBoard(true,this)
  private var turnOwner : TurnOwner = TurnOwner.Player
  private var loadingMessage : Stage = _

  title = TITLE
  icons += new Image("/assets/icon.png")
  Ia.start()
  GameManager.addObserver(this)
  TurnManager.addObserver(this)
  CardCreator.setController(controller)
  PopupBuilder.setController(controller)
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
      children = Seq(iABoard, humanBoard)
    }, zoomZone)

    loadingMessage = PopupBuilder.openLoadingScreen(this.window.value)
    loadingMessage.show()
  }

  resizable = true
  sizeToScene()
  show()

  onCloseRequest = _ => Ia.interrupt()

  override def update(event: Events.Event): Unit = event match {
    case event if  event.isInstanceOf[BuildGameField] => {
      humanBoard.myBoard = event.asInstanceOf[BuildGameField].playerBoard
      humanBoard.opponentBoard = event.asInstanceOf[BuildGameField].opponentBoard
      iABoard.myBoard = event.asInstanceOf[BuildGameField].opponentBoard
      iABoard.opponentBoard = event.asInstanceOf[BuildGameField].playerBoard
      Platform.runLater(humanBoard.updateHand())
      Platform.runLater(humanBoard.updateActive())
      Platform.runLater(PopupBuilder.closeLoadingScreen(loadingMessage))
    }
    case event if event.isInstanceOf[FlipCoin] =>{
      turnOwner = event.asInstanceOf[FlipCoin].coinValue
    }
    case event : UpdateBoards => {
      Platform.runLater({
        humanBoard.updateActive()
        humanBoard.updateHand()
        humanBoard.updateBench()
        humanBoard.updateDiscardStack()
        if (!humanBoard.isFirstTurn) {
          iABoard.updateBench()
          iABoard.updateActive()
          iABoard.updateDiscardStack()
        }
      })
    }
    case event : NextTurn => {
      humanBoard.disable = !(event.turnOwner == TurnOwner.Player)
      if(event.turnOwner == TurnOwner.Player) {
        controller.activePokemonStatusCheck()
        Platform.runLater({
            PopupBuilder.openTurnScreen(this)
            controller.drawACard()
            humanBoard.updateHand()
          })
      }
      Platform.runLater({
        iABoard.updateBench()
        iABoard.updateActive()
        iABoard.updateDiscardStack()
      })
    }
    case event : PokemonKO => {
      println("pokemonKO event")
      if (humanBoard.myBoard.activePokemon.get.isKO)
        Platform.runLater(PopupBuilder.openBenchSelectionScreen(this,humanBoard.myBoard.pokemonBench, event.isAttackingPokemonKO))
      else {
        controller.drawAPrizeCard()
      }
    }
    case _ =>
  }
}
