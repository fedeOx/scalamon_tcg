package view

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.BorderPane

object StartGameScene extends Scene {
  val cssStyle = List(getClass.getResource("/style/startGameGui.css").toExternalForm)
  stylesheets = cssStyle

  root = new BorderPane {
    id = "startGame-pane"
    bottom = new Button {
      id = "startGame-btn"
      text = "Select Deck"
      onAction = _ => {
        StartGameGui.getPrimaryStage().width = 600
        StartGameGui.getPrimaryStage().height = 700
        StartGameGui.getPrimaryStage().scene = DeckSelection
      }
    }
  }
}
