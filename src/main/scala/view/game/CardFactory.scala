package view.game

import common.TurnOwner
import controller.Controller
import model.game.{Board, StatusType}
import scalafx.scene.Node
import scalafx.scene.image.Image
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.{Box, DrawMode}
import scalafx.stage.Window
import view.PopupBuilder
import view.game.CardType.CardType

trait CardFactory {
  /**
   * Sets the object's controller field
   * @param c: the controller
   */
  def setController(c: Controller): Unit

}

/**
 * Object that creates cards
 */
object CardFactory extends CardFactory {
  private var controller: Controller = _

  def setController(c: Controller): Unit = {
    controller = c
  }

  abstract class GraphicCard() extends Box {
    def cardWidth : Double
    def cardHeight: Double
    def cardMaterial : PhongMaterial
  }

  def apply(cardType: CardType, cardImage: String,zoomZone: Option[ZoomZone] = Option.empty, transX: Double = 0,
            cardIndex: Int = 0, isHumans: Option[Boolean] = Option.empty, zone: Option[Node] = Option.empty,
            board: Option[Board] = Option.empty, gameWindow: Option[Window] = Option.empty): GraphicCard = cardType match {
    case CardType.Active => new ActiveCard(cardImage,zoomZone,cardIndex,isHumans,zone,
      board,gameWindow)
    case CardType.Bench => new BenchCard(cardImage,zoomZone,cardIndex,isHumans,zone,
      board,gameWindow)
    case CardType.Hand => new HandCard(cardImage,zoomZone,cardIndex,isHumans,zone,
      board,gameWindow)
    case CardType.Deck => new DeckCard(cardImage,board)
    case _ => new StaticCard(cardImage)
  }

  private class ActiveCard(cardImage: String,zoomZone: Option[ZoomZone] = Option.empty,
                           cardIndex: Int = 0, isHumans: Option[Boolean] = Option.empty, zone: Option[Node] = Option.empty,
                           board: Option[Board] = Option.empty, gameWindow: Option[Window] = Option.empty) extends GraphicCard {
    val cardWidth = 8
    val cardHeight = 11.2
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(cardImage)
    var isSelected: Boolean = false
    width = cardWidth
    height = cardHeight
    depth = 0.5

    board.get.activePokemon.get.status match {
      case StatusType.Asleep => rotate = -90
      case StatusType.Confused => rotate = 180
      case StatusType.Paralyzed => rotate = 90
      case _ =>
    }
    material = cardMaterial

    onMouseEntered = _ => {
      zoomZone.get.showContent(board.get.activePokemon.get)
    }
    onMouseExited = _ => {
      if (zoomZone.isDefined)
        zoomZone.get.children.remove(0,zoomZone.get.children.size())
    }
    if (isHumans.isDefined && isHumans.get)
      addAction(this, CardType.Active, zone = zone, gameWindow = gameWindow)
    drawMode = DrawMode.Fill
  }

  private class BenchCard(cardImage: String,zoomZone: Option[ZoomZone] = Option.empty,
                          cardIndex: Int = 0, isHumans: Option[Boolean] = Option.empty, zone: Option[Node] = Option.empty,
                          board: Option[Board] = Option.empty, gameWindow: Option[Window] = Option.empty) extends GraphicCard {
    val cardWidth = 5.5
    val cardHeight = 7.7
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(cardImage)
    var isSelected: Boolean = false
    width = cardWidth
    height = cardHeight
    depth = 0.5
    material = cardMaterial

    onMouseEntered = _ => {
      zoomZone.get.showContent(board.get.pokemonBench(cardIndex).get)
    }
    onMouseExited = _ => {
      if (zoomZone.isDefined)
        zoomZone.get.children.remove(0,zoomZone.get.children.size())
    }
    if (isHumans.isDefined && isHumans.get)
      addAction(this, CardType.Bench, cardIndex, zone = zone, gameWindow = gameWindow)
    drawMode = DrawMode.Fill
  }
  
  private class HandCard(cardImage: String,zoomZone: Option[ZoomZone] = Option.empty,
                         cardIndex: Int = 0, isHumans: Option[Boolean] = Option.empty, zone: Option[Node] = Option.empty,
                         board: Option[Board] = Option.empty, gameWindow: Option[Window] = Option.empty) extends GraphicCard {
    val cardWidth = 5.5
    val cardHeight = 7.7
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(cardImage)
    var isSelected: Boolean = false
    width = cardWidth
    height = cardHeight
    depth = 0.5
    material = cardMaterial

    translateX = -cardIndex

    onMouseEntered = _ => {
      zoomZone.get.showContent(board.get.hand(cardIndex))
    }
    onMouseExited = _ => {
      if (zoomZone.isDefined)
        zoomZone.get.children.remove(0,zoomZone.get.children.size())
    }
    if (isHumans.isDefined && isHumans.get)
      addAction(this, CardType.Hand, cardIndex, board = board, gameWindow = gameWindow)
    drawMode = DrawMode.Fill
  }
  private class DeckCard(cardImage: String, board: Option[Board]) extends GraphicCard {
    val cardWidth = 5.5
    val cardHeight = 7.7
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(cardImage)
    width = cardWidth
    height = cardHeight
    if(board.isDefined)
      depth = 0.45+(0.05*(board.get.deck.size/2))
    else
      depth = 1.5
    material = cardMaterial
    drawMode = DrawMode.Fill
  }


  private class StaticCard(cardImage: String) extends GraphicCard {
    val cardWidth = 5.5
    val cardHeight = 7.7
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(cardImage)
    width = cardWidth
    height = cardHeight
    depth = 0.5
    material = cardMaterial
    drawMode = DrawMode.Fill
  }

  private def addAction(card: Box, cardType: String, cardIndex: Int = 0, zone: Option[Node] = Option.empty,
                        board: Option[Board] = Option.empty, gameWindow: Option[Window] = Option.empty): Unit = cardType match {
    case CardType.Active => card.onMouseClicked = _ => {
      activeAction(gameWindow, zone)
    }
    case CardType.Bench => card.onMouseClicked = _ => {
      benchAction(gameWindow, zone, cardIndex)
    }
    case CardType.Hand => card.onMouseClicked = _ => {
      handAction(gameWindow, cardIndex, card, board)
    }
    case _ =>
  }

  private def activeAction(gameWindow: Option[Window],zone: Option[Node]) : Unit = {
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

  private def benchAction(gameWindow: Option[Window],zone: Option[Node], cardIndex: Int) : Unit = {
    if (gameWindow.get.asInstanceOf[GameBoardView].turnOwner.equals(TurnOwner.Player)) {
      zone.get.asInstanceOf[BenchZone].cardClicked = true
      try {
        controller.selectBenchLocation(cardIndex)
      } catch {
        case ex: Exception => PopupBuilder.openInvalidOperationMessage(gameWindow.get, ex.getMessage)
      }
    }
  }

  private def handAction(gameWindow: Option[Window], cardIndex: Int, card: Box, board: Option[Board]): Unit = {
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
}