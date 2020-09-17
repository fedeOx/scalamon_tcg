package view

import controller.Controller
import model.game.Board
import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.image.Image
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.{Box, DrawMode}
import scalafx.scene.transform.Rotate
import scalafx.scene.transform.Transform._

/***
 * Object that creates cards
 */
object CardCreator {
  private val controller = Controller()
  private def addAction(card: Box, cardType: String, cardIndex: Int, zone: Option[Node] = Option.empty,
                        board: Option[Board] = Option.empty): Unit = cardType match {
    case CardType.Active => card.onMouseClicked = _ => {
      println("active")
      //if noCardSelected && active.canDoAction
        zone.get.asInstanceOf[ActivePkmnZone].openMenu()
      // else
      //controller.selectActivePokemonLocation()
    }
    case CardType.Bench => {
      card.onMouseClicked = _ => println("bench " + cardIndex)
    }
    case CardType.Hand => card.onMouseClicked = _ => {
      println("hand " + cardIndex)
      //canBeSelected
      if (card.width.value != 5.8) {
        //zone.get.asInstanceOf[HandZone].updateView()
        card.translateZ = -0.5
        println("to front")
        println(card.getParent.getClass.getName)

        card.width = 5.8
        println(card.width.value)
        card.height = 8
        controller.selectCardFromHand(board.get.hand(cardIndex))
      } else if (card.width.value == 5.8) {
        println("to back")
        card.translateZ = 0
        card.width = 5.5
        card.height = 7.7
        //TODO: controller.deselectCardFromHand(board.get.hand(cardIndex))
      }
    }
    case _ =>
  }

  def createCard(cardImage: String, zoomZone: Option[ZoomZone] = Option.empty, cardType: String, transX: Double = 0,
                 cardIndex: Int = 0, isHumans: Option[Boolean] = Option.empty, zone: Option[Node] = Option.empty,
                 board: Option[Board] = Option.empty): Box = {
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
        addAction(this, cardType, cardIndex, zone, board)
      drawMode = DrawMode.Fill
    }
  }
}



