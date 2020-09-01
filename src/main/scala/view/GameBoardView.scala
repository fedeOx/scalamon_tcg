package view

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.image.Image
import scalafx.scene.{Group, Scene}
import scalafx.scene.layout.{Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize, GridPane, Pane}
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle

class GameBoardView extends JFXApp.PrimaryStage {
  private val WIDTH = 1600
  private val HEIGHT = 900
  private val TITLE = "Scalamon"
  /*private val utilityPanel: UtilityPanel = new UtilityPanel
  private val boardAndPlayerPanel: BoardAndPlayerPanel = new BoardAndPlayerPanel(cards)
  private val legendPanel = new LegendPanel(users)*/
  title = TITLE
  scene = new Scene(WIDTH, HEIGHT) {
    stylesheets = List("/style/PlayerBoardStyle.css")
    root = new Pane() {
      styleClass += "body"
      jfxBackgroundImage2sfx(new BackgroundImage(new Image("/assets/playmat.jpg"),BackgroundRepeat.NoRepeat,
        BackgroundRepeat.NoRepeat, BackgroundPosition.Default, BackgroundSize.Default))
      children = new GridPane() {

        add(new Rectangle{

          width = 1600
          height = 450
          fill <== when(hover) choose Green otherwise Transparent
        },0,0)
        add(new HumanPlayerBoard, 0, 1)
      }
    }
  }

  resizable = false
  sizeToScene()
  show()
}
