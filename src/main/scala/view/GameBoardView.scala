package view

import common.{Observer, TurnOwner}
import common.TurnOwner.TurnOwner
import javafx.scene.paint.ImagePattern
import model.core.{GameManager, TurnManager}
import model.event.Events
import model.event.Events.Event.{BuildGameField, FlipCoin}
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout._
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{Group, PerspectiveCamera, Scene, SceneAntialiasing}
import scalafx.stage.Window

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
  title = TITLE
  GameManager.addObserver(this)
  TurnManager.addObserver(this)
  println("sono gameboardview e mi registro")
  scene = new Scene(WIDTH, HEIGHT, true, SceneAntialiasing.Balanced) {
    println(parentWindow)
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
  }

  resizable = true
  sizeToScene()
  show()

  override def update(event: Events.Event): Unit = event match {
    case event if  event.isInstanceOf[BuildGameField] => {
      val gameField =  event.asInstanceOf[BuildGameField].gameField
      humanBoard.board = gameField.playerBoard
      opponentBoard.board = gameField.opponentBoard
      Platform.runLater(humanBoard.updateHand())
    }
    case event if event.isInstanceOf[FlipCoin] =>{
      turnOwner = event.asInstanceOf[FlipCoin].coinValue
      println("turno di : "+turnOwner)
    }
  }


}
