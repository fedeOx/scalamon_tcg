package view

import controller.Controller
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.BorderPane

case class StartGameView(controller: Controller) extends Scene {

  val cssStyle = List(getClass.getResource("/style/startGameGui.css").toExternalForm)
  stylesheets = cssStyle

  root = new BorderPane {
    id = "startGame-pane"
    bottom = new Button {
      id = "startGame-btn"
      text = "Select Deck"
      onAction = _ => {

        GameLauncher.stage.scene =  DeckSelection(controller)
        GameLauncher.stage.width = 1400
        GameLauncher.stage.height = 1000
        GameLauncher.stage.x = 200
        GameLauncher.stage.y = 20
      }
    }
  }
}
