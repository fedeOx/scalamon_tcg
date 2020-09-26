package view

import common.TurnOwner
import controller.Controller
import javafx.animation.Animation.Status
import model.game.{Board, StatusType}
import scalafx.scene.Node
import scalafx.scene.image.Image
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.{Box, DrawMode}
import scalafx.stage.Window

/***
 * Object that creates cards
 */
object CardCreator {
  private var controller: Controller = _

  def setController(c: Controller): Unit = {
    controller = c
  }
  private def addAction(card: Box, cardType: String, cardIndex: Int, zone: Option[Node] = Option.empty,
                        board: Option[Board] = Option.empty, gameWindow: Option[Window] = Option.empty): Unit = cardType match {
    case CardType.Active => card.onMouseClicked = _ => {
      if (gameWindow.get.asInstanceOf[GameBoardView].turnOwner.equals(TurnOwner.Player)) {
        if (controller.handCardSelected.isEmpty)
          zone.get.asInstanceOf[ActivePkmnZone].openMenu()
        else {
          try {
            controller.selectActivePokemonLocation()
          } catch {
            case ex: Exception => PopupBuilder.openInvalidOperationMessage(gameWindow.get,ex.getMessage)
          }
        }
      }
    }
    case CardType.Bench => {
      card.onMouseClicked = _ => {
        if (gameWindow.get.asInstanceOf[GameBoardView].turnOwner.equals(TurnOwner.Player)) {
          try {
            controller.selectBenchLocation(cardIndex)
          } catch {
            case ex: Exception => PopupBuilder.openInvalidOperationMessage(gameWindow.get, ex.getMessage)
          }
        }
      }
    }
    case CardType.Hand => card.onMouseClicked = _ => {
      if (gameWindow.get.asInstanceOf[GameBoardView].turnOwner.equals(TurnOwner.Player)) {
        if (card.width.value != 5.8) {
          card.translateZ = -0.5
          card.width = 5.8
          card.height = 8
          try {
            controller.handCardSelected = Some(board.get.hand(cardIndex))
          } catch {
            case ex: Exception => PopupBuilder.openInvalidOperationMessage(gameWindow.get, ex.getMessage)
          }
        } else if (card.width.value == 5.8) {
          card.translateZ = 0
          card.width = 5.5
          card.height = 7.7
          try {
            controller.handCardSelected = Option.empty
          } catch {
            case ex: Exception => PopupBuilder.openInvalidOperationMessage(gameWindow.get, ex.getMessage)
          }
        }
      }
    }
    case _ =>
  }

  def createCard(cardImage: String, zoomZone: Option[ZoomZone] = Option.empty, cardType: String, transX: Double = 0,
                 cardIndex: Int = 0, isHumans: Option[Boolean] = Option.empty, zone: Option[Node] = Option.empty,
                 board: Option[Board] = Option.empty, gameWindow: Option[Window] = Option.empty): Box = {
    val normalCardWidth = 5.5
    val normalCardHeight = 7.7
    val activeCardWidth = 8
    val activeCardHeight = 11.2
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(cardImage)
    new Box {
      var isSelected: Boolean = false
      if (cardType.equals(CardType.Active)) width = activeCardWidth else width = normalCardWidth //6
      if(cardType.equals(CardType.Active)) height =  activeCardHeight else height = normalCardHeight //8.4
      depth = 0.5

      if (cardType.equals(CardType.Active)) {
        board.get.activePokemon.get.status match {
          case StatusType.Asleep => rotate = -90
          case StatusType.Confused => rotate = 180
          case StatusType.Paralyzed => rotate = 90
          case _ =>
        }
      }

      translateX = -transX
      material = cardMaterial

      onMouseEntered = _ => {
        if (zoomZone.isDefined) {
          cardType match {
            case cardType if cardType.equals(CardType.Hand) => zoomZone.get.showContent(board.get.hand(cardIndex))
            case cardType if cardType.equals(CardType.Bench) => zoomZone.get.showContent(board.get.pokemonBench(cardIndex).get)
            case cardType if cardType.equals(CardType.Active) => zoomZone.get.showContent(board.get.activePokemon.get)
          }
        }
        if (cardType.equals(CardType.Bench)) {
          zone.get.asInstanceOf[BenchZone].isOverChildren = true
        }
      }
      onMouseExited = _ => {
        if (zoomZone.isDefined)
          zoomZone.get.children.remove(0,zoomZone.get.children.size())
        if (cardType.equals(CardType.Bench)) {
          zone.get.asInstanceOf[BenchZone].isOverChildren = false
        }
      }
      if (isHumans.isDefined && isHumans.get)
        addAction(this, cardType, cardIndex, zone, board, gameWindow)
      drawMode = DrawMode.Fill
    }
  }
}



