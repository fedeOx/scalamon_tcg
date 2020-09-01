import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.{Group, Scene}
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import view.GameBoardView

object GameLauncher extends JFXApp {
  val gameBoard = new GameBoardView
  //gameBoard.show()
}
  /*stage = new JFXApp.PrimaryStage {




    title.value = "Game"
    width = 1600
    height = 900
    scene = new Scene {
      fill = LightGreen
      val gameGrid = new GridPane()
      gameGrid.addRow(0, Rectangle.sfxRectangle2jfx(new Rectangle {
        x = 0
        y = 0
        width = 1600
        height = 450
        fill <== when(hover) choose Green otherwise Red
      }))
      gameGrid.addRow(1, Rectangle.sfxRectangle2jfx(new Rectangle {
        x = 0
        y = 450
        width = 1600
        height = gameGrid.height.value / 2
        fill <== when(hover) choose Red otherwise Green
      }))
      content = gameGrid
    }*/