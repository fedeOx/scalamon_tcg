package view

import javafx.scene.paint.ImagePattern
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout._
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{Group, PerspectiveCamera, Scene, SceneAntialiasing}

/** *
 * Stage that contains the game scene
 */
class GameBoardView extends JFXApp.PrimaryStage {
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"

  title = TITLE
  scene = new Scene(WIDTH, HEIGHT, true, SceneAntialiasing.Balanced) {
    println(DeckSelection.getChosenDeck)
    stylesheets = List("/style/PlayerBoardStyle.css")
    camera = new PerspectiveCamera(true) {
      transforms += (
        new Rotate(50, Rotate.XAxis),
        new Translate(0, 5, -75)) //-75))
    }
    val playMatMaterial = new PhongMaterial()
    playMatMaterial.diffuseMap = new Image("/assets/playmat.png")
    val zoomZone = new ZoomZone
    fill = new ImagePattern(new Image("/assets/woodTable.png"))
    content = new Group {
      children = Seq(new Box {
        width = 55
        height = 50
        depth = 0.4
        material = playMatMaterial
      }, new VBox() {
        translateX = -27.5
        translateY = -25
        minHeight = 50
        minWidth = 55
        alignment = Pos.TopCenter
        children = Seq(new PlayerBoard(false, zoomZone),
          new PlayerBoard(true, zoomZone))
      }, zoomZone)
    }
  }

  resizable = true
  sizeToScene()
  show()
}
