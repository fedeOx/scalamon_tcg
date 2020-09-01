package view

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.{Group, Scene}
import scalafx.scene.layout.GridPane
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
    root = new Group() {
      children = new GridPane() {
        add(new Rectangle{

          width = 1600
          height = 450
          fill <== when(hover) choose Green otherwise Transparent
        },0,0)
        /*add(new Rectangle{

          width = 1600
          height = 450
          fill <== when(hover) choose Red otherwise Transparent
        },0,1)*/
        add(new HumanPlayerBoard, 0, 1)
        /*add(utilityPanel, 2, 0)
        add(boardAndPlayerPanel, 0, 0)*/
      }
    }
  }

  resizable = false
  sizeToScene()
  show()
}
