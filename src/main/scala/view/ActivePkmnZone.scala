package view

import javafx.geometry.Insets
import model.exception.InvalidOperationException
import model.game.Cards.PokemonCard
import model.game.StatusType
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import scalafx.stage.{Modality, Stage}
import view.CardCreator._

/**
 * Field Zone that contains the active Pokémon
 */
trait ActivePkmnZone extends HBox {
  /**
   * Updates the HBox children
   * @param active: the card to visualize
   */
  def updateView(active: Option[PokemonCard] = Option.empty): Unit

  /**
   * Opens the menu for attacks and retreat
   */
  def openMenu(): Unit
}

object ActivePkmnZone {
  /**
   * Creates an ActivePkmnZone
   * @param isHumans: true if it's the human ActivePkmnZone
   * @param board: the parent PlayerBoard
   * @return an instance of ActivePkmnZone
   */
  def apply(isHumans: Boolean, board: PlayerBoard): ActivePkmnZone = ActivePkmnZoneImpl(isHumans, board)

  private case class ActivePkmnZoneImpl(isHumans: Boolean, board: PlayerBoard) extends ActivePkmnZone {
    private val WIDTH = 30
    private val HEIGHT = 15
    private var isEmpty: Boolean = _
    private val parentBoard = board
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
        children = createCard("/assets/" + active.get.belongingSetCode + "/" + active.get.imageId + ".jpg", Some(board.gameWindow.asInstanceOf[GameBoardView].zoomZone),
          cardType = CardType.Active, isHumans = Some(isHumans), zone = Some(this), board = Some(parentBoard.myBoard), gameWindow = Some(board.gameWindow))
      }
    }

    def openMenu(): Unit = {
      val dialog: Stage = new Stage() {
        initOwner(board.gameWindow)
        initModality(Modality.ApplicationModal)
        scene = new Scene(200, 300) {
          stylesheets = List("/style/PlayerBoardStyle.css")
          private val buttonContainer = new VBox {
            prefWidth = 200
            alignment = Pos.TopCenter
            styleClass += "activeMenu"
            fill = Color.LightGrey
          }
          private var buttons: Seq[Button] = Seq()
          parentBoard.myBoard.activePokemon.get.attacks.foreach(f = attack => {
            buttons = buttons :+ new Button(attack.name) {
              text = attack.name
              prefHeight = 50
              prefWidth = 160
              margin = new Insets(10, 0, 0, 0)
              styleClass += "activeMenuButton"
              if (!parentBoard.myBoard.activePokemon.get.hasEnergies(attack.cost) || parentBoard.myBoard.activePokemon.get.status.equals(StatusType.Asleep))
                disable = true
              onAction = event => {
                event.getSource.asInstanceOf[javafx.scene.control.Button].scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
                try {
                  board.gameWindow.asInstanceOf[GameBoardView].controller.declareAttack(parentBoard.myBoard, parentBoard.opponentBoard, attack)
                } catch {
                  case ex: InvalidOperationException => PopupBuilder.openInvalidOperationMessage(parentBoard.gameWindow, ex.getMessage)
                }
              }
            }
          })
          buttons = buttons :+ new Button("Retreat") {
            prefHeight = 50
            prefWidth = 160
            margin = new Insets(10, 0, 0, 0)
            styleClass += "activeMenuButton"
            if (parentBoard.myBoard.activePokemon.get.totalEnergiesStored < parentBoard.myBoard.activePokemon.get.retreatCost.size
              || parentBoard.myBoard.pokemonBench.head.isEmpty || parentBoard.myBoard.activePokemon.get.status.equals(StatusType.Asleep))
              disable = true
            onAction = event => {
              println("ritirata")
              PopupBuilder.openBenchSelectionScreen(board.gameWindow, parentBoard.myBoard.pokemonBench, isAttackingPokemonKO = false)

              event.getSource.asInstanceOf[javafx.scene.control.Button].scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
            }
          }
          buttonContainer.children = buttons
          content = buttonContainer
        }
        sizeToScene()
        x = board.gameWindow.getX + board.gameWindow.getWidth / 1.6
        y = board.gameWindow.getY + board.gameWindow.getHeight / 2.8
        resizable = false
      }
      dialog.showAndWait()
    }

    prefWidth = WIDTH
    prefHeight = HEIGHT
    maxHeight = HEIGHT
    styleClass += "active"
    if (isHumans)
      translateX = 13
    else
      translateX = 12
    alignment = Pos.Center

    onMouseClicked = _ => {
      if (isHumans && isEmpty) {
        try {
          board.gameWindow.asInstanceOf[GameBoardView].controller.selectActivePokemonLocation()
        } catch {
          case ex: Exception => PopupBuilder.openInvalidOperationMessage(board.gameWindow, ex.getMessage)
        }

      }
    }
  }
}

