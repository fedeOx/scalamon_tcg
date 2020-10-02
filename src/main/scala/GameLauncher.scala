package view

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

object GameLauncher extends JFXApp {
  stage = new PrimaryStage {
    title = "ScalaMon TCG"
    width = 500
    height = 300
    scene = StartGameScene()
  }
}

