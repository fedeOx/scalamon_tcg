package view

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.BorderPane

import scala.io.Source


object StartGameGui extends JFXApp {

  val cssStyle = getClass.getResource("/style/startGameGui.css").toExternalForm
  val startGameBtn : Button = new Button {
    id = "startGame-btn"
    text = "Start Game"
    onAction = () => {
      stage = DeckSelection
    }
  }


  stage = new PrimaryStage {
    title = "ScalaMon TCG"
    width = 500
    height = 300
    scene = new Scene {
      stylesheets += cssStyle
      root = new BorderPane {
        id = "startGame-pane"
        bottom = startGameBtn
      }
    }
  }
}
