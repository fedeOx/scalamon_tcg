package view

import controller.Controller
import model.core.DataLoader
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

        GameLauncher.stage.scene = new DeckSelection(controller)
        GameLauncher.stage.width = 1400
        GameLauncher.stage.height = 1000
        GameLauncher.stage.x = 200
        GameLauncher.stage.y = 20
      }
    }
  }
}
