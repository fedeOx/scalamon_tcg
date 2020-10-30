package app

import controller.Controller
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import view.StartGameView

object GameLauncher extends JFXApp {

  stage = new PrimaryStage {
    title = "ScalaMon TCG"
    width = 500
    height = 300
    val controller : Controller = Controller()
    scene = StartGameView(controller)
    onCloseRequest = _ => controller.resetGame()
  }
}
