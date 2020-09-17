package view

import javafx.geometry.Insets
import model.game.Cards.PokemonCard
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ButtonType}
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
import model.pokemonEffect.Effect

/***
 * The field zone that contains the active pokemon
 * @param zone: the zone for the zoomed cards
 * @param isHumans: true if it's the human's board
 * @param board: the board where the zone is located
 * @param parentWindow: the parent window of this component
 */
case class ActivePkmnZone(zone: ZoomZone, isHumans: Boolean, board: PlayerBoard, parentWindow: Window) extends HBox{
  private val WIDTH = 32
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
        private var buttonContainer = new VBox {
          prefWidth = 200
          fill = Color.Blue
          alignment = Pos.TopCenter
        }
        private var buttons : Seq[Button] = Seq()
        parentBoard.board.activePokemon.get.attacks.foreach(attack => {
          buttons = buttons :+ new Button(attack.name) {
            prefHeight = 50
            prefWidth = 160
            margin = new Insets(10,0,0,0)
            if (!parentBoard.board.activePokemon.get.hasEnergies(attack.cost))
              disable = true
            onAction = event => {
              println("attacco con " + attack.name)
              utils.controller.declareAttack(attack)
              event.getSource.asInstanceOf[javafx.scene.control.Button].scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
              //dialog.close()
            }
          }
        })
        buttons = buttons :+ new Button("Retreat") {
          prefHeight = 50
          prefWidth = 160
          margin = new Insets(10,0,0,0)
          if (parentBoard.board.activePokemon.get.totalEnergiesStored < parentBoard.board.activePokemon.get.retreatCost.size)
            disable = true
          onAction = event => {
            println("ritirata")
            event.getSource.asInstanceOf[javafx.scene.control.Button].scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
          }
        }
        buttonContainer.children = buttons
        content = buttonContainer
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
  maxHeight = HEIGHT
  styleClass += "active"
  translateX = 10
  alignment = Pos.Center

  onMouseClicked = _ => {
    if (isHumans && isEmpty) {
      utils.controller.selectActivePokemonLocation()
    }
  }

}
