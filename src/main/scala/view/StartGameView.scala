package view

import app.GameLauncher
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
        GameLauncher.stage.close()
        new DeckSelection(controller)
      }
    }
  }
}
