import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.{Group, Scene}
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import view.GameBoardView

object GameLauncher extends JFXApp {
  val gameBoard = new GameBoardView
}