package view

import javafx.geometry.Insets
import model.game.Cards.PokemonCard
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import scalafx.stage.{Modality, Stage, Window}
import view.CardCreator._
import _root_.controller.Controller
import model.game.EnergyType.EnergyType
import model.game.Weakness.Operation
import model.game.Weakness.Operation.Operation
import model.game.{EnergyType, Resistance, Weakness}

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
  private var isEmpty : Boolean = _
  private val parentBoard = board
  private val controller = Controller()
  updateView()

  def updateView(active: Option[PokemonCard] = Option.empty): Unit = {
    if (active.isEmpty) {
      isEmpty = true
      val cardMaterial = new PhongMaterial()
      cardMaterial.diffuseColor = Color.Transparent
      children = new Box {
        material = cardMaterial
        depth = 0.5
      }
    } else {
      isEmpty = false
      children = createCard("/assets/base1/"+active.get.imageId+".jpg", Some(zone), cardType = CardType.Active, isHumans = Some(isHumans), zone = Some(this),
        board = Some(parentBoard.board))
      println(active)
    }
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

  onMouseClicked = _ => {
    if (isHumans && isEmpty) {
      println("activePkmnZone")
      //controller.selectActivePokemonLocation()
      val weakness: Weakness = new Weakness {
        override def energyType: EnergyType = EnergyType.Fighting
        override def operation: Operation = Operation.multiply2
      }
      val resistance: Resistance = new Resistance {
        override def energyType: EnergyType = EnergyType.Lightning
        override def reduction: Int = 30
      }
      var carta = PokemonCard("4", "base1",Seq(EnergyType.Colorless), "pokemonName", 100, Seq(weakness),
        Seq(resistance), Seq(EnergyType.Colorless, EnergyType.Colorless), "", Nil)
      controller.addActivePokemon(carta)
    }
  }

}
