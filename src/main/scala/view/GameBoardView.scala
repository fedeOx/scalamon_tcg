package view

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout._
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{Group, PerspectiveCamera, Scene, SceneAntialiasing}

class GameBoardView extends JFXApp.PrimaryStage {
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"
  /*private val utilityPanel: UtilityPanel = new UtilityPanel
  private val boardAndPlayerPanel: BoardAndPlayerPanel = new BoardAndPlayerPanel(cards)
  private val legendPanel = new LegendPanel(users)*/
  title = TITLE
  scene = new Scene(WIDTH, HEIGHT, true, SceneAntialiasing.Balanced) {
    stylesheets = List("/style/PlayerBoardStyle.css")
    camera = new PerspectiveCamera(true) {
      transforms += (
        new Rotate(50, Rotate.XAxis),
        new Translate(0, 5, -75))
    }
    val imgPlayMat: Image = new Image(getClass().getResource("/assets/playmat.png").toExternalForm())
    val playMatMaterial = new PhongMaterial()
    playMatMaterial.diffuseMap = imgPlayMat
    content = new Group {
      onMouseEntered = _ => println("sono il gruppo")
      children = Seq(new Box {
        width = 55
        height = 50
        depth = 0.4
        material = playMatMaterial
        onMouseEntered = _ => println("sono il tavolo")
      }, new VBox() {
        onMouseEntered = _ => println("sono il vbox")
        translateX = -27.5
        translateY = -25
        minHeight = 50
        minWidth= 55
        //background = new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(Color.Blue, null, null))
        alignment = Pos.TopCenter
        children = Seq(new HumanPlayerBoard(false),
          new HumanPlayerBoard(true))
        /*children = Seq(cardCreator.cardPosition(new Image("/assets/1.jpg")),
          cardCreator.cardPosition(new Image("/assets/1.jpg")),
          cardCreator.cardPosition(new Image("/assets/1.jpg")),
          cardCreator.cardPosition(new Image("/assets/1.jpg")),//HumanPlayerBoard(false),
        )//new HumanPlayerBoard(true))*/
      })
    }


  }

  resizable = false
  sizeToScene()
  show()
}
