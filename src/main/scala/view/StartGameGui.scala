package view

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

object StartGameGui extends JFXApp {
  stage = new PrimaryStage {
    title = "ScalaMon TCG"
    width = 500
    height = 300
    scene = StartGameScene
  }

  //TODO: togliere
  def getPrimaryStage: PrimaryStage = {
    stage
  }
}

