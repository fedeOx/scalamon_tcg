package view

import controller.Controller
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.BorderPane

object StartGameScene extends Scene {
  val cssStyle = List(getClass.getResource("/style/startGameGui.css").toExternalForm)
  val controller : Controller = Controller()
  stylesheets = cssStyle

  root = new BorderPane {
    id = "startGame-pane"
    bottom = new Button {
      id = "startGame-btn"
      text = "Select Deck"
      onAction = _ => {

        StartGameGui.getPrimaryStage.scene = new DeckSelection
        StartGameGui.getPrimaryStage.width = 1400
        StartGameGui.getPrimaryStage.height = 1000
        StartGameGui.getPrimaryStage.x = 200
        StartGameGui.getPrimaryStage.y = 20
      }
    }
  }
}
