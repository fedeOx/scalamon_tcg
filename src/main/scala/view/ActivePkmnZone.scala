package view

import javafx.geometry.Insets
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.{Modality, Stage, Window}
import view.CardCreator._

/***
 * The field zone that contains the active pokemon
 * @param zone: the zone for the zoomed cards
 * @param isHumans: true if it's the human's board
 * @param board: the board where the zone is located
 * @param parentWindow: the parent window of this component
 */
case class ActivePkmnZone(zone: ZoomZone, isHumans: Boolean, board: PlayerBoard, parentWindow: Window) extends HBox{
  private val WIDTH = 35
  private val HEIGHT = 15

  updateView()

  def updateView(): Unit = {
    children = createCard("/assets/4.jpg", Some(zone), cardType = CardType.active, isHumans = Some(isHumans), zone = Some(this))
  }

  def openMenu() : Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parentWindow)
      initModality(Modality.ApplicationModal)
      scene = new Scene(200,300) {
        content = new VBox {
          prefWidth = 200
          fill = Color.Blue
          alignment = Pos.TopCenter
          children = List(new Button("Energy Burn") {
            prefHeight = 50
            prefWidth = 160
            margin = new Insets(10,0,0,0)
          }, new Button("Fire Spin") {
            prefHeight = 50
            prefWidth = 160
            margin = new Insets(10,0,0,0)
          }, new Button("Retreat") {
            prefHeight = 50
            prefWidth = 160
            margin = new Insets(10,0,0,0)
          })
        }
      }
      sizeToScene()
      x = parentWindow.getX + parentWindow.getWidth / 1.6
      y = parentWindow.getY + parentWindow.getHeight / 2.8
      resizable = false
    }
    dialog.showAndWait()
  }
  prefWidth = WIDTH
  prefHeight = HEIGHT
  styleClass += "active"
  translateX = 10
  alignment = Pos.Center

}
