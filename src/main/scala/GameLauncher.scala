import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle

object GameLauncher extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "Game"
    width = 1600
    height = 900
    scene = new Scene {
      fill = LightGreen
      content = new Rectangle {
        x = 0
        y = 0
        width = stage.width.value
        height = stage.height.value/2
        fill <== when(hover) choose Green otherwise Red
      }
      content.add(Rectangle.sfxRectangle2jfx(new Rectangle {
        x = 0
        y = stage.height.value/2
        width = stage.width.value
        height = stage.height.value/2
        fill <== when(hover) choose Red otherwise Green
      }))
    }
  }
}