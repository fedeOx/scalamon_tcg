package view

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
  private def addAction(card: Box, cardType: String, cardIndex: Int, zone: Option[Node] = Option.empty): Unit = cardType match {
    case CardType.active => card.onMouseClicked = _ => {
      println("active")
      //if noCardSelected && active.canDoAction
      zone.get.asInstanceOf[ActivePkmnZone].openMenu()
    }
    case CardType.bench => card.onMouseClicked = _ => println("bench " + cardIndex)
    case CardType.hand => card.onMouseClicked = _ => {
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
      } else if (card.width.value == 5.8) {
        println("to back")
        card.translateZ = 0
        card.width = 5.5
        card.height = 7.7
      }
    }
    case _ =>
  }

  def createCard(cardImage: String, zoomZone: Option[ZoomZone] = Option.empty, cardType: String, transX: Double = 0,
                 cardIndex: Int = 0, isHumans: Option[Boolean] = Option.empty, zone: Option[Node] = Option.empty): Box = {
    val normalCardWidth = 5.5
    val normalCardHeight = 7.7
    val activeCardWidth = 8
    val activeCardHeight = 11.2
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = new Image(cardImage)
    new Box {
      var isSelected: Boolean = false
      if (cardType.equals(CardType.active)) width = activeCardWidth else width = normalCardWidth //6
      if(cardType.equals(CardType.active)) height =  activeCardHeight else height = normalCardHeight //8.4
      depth = 0.5

      translateX = -transX
      material = cardMaterial

      onMouseEntered = _ => {
        if (zoomZone.isDefined) {
          zoomZone.get.children = new Box {
            depth = 0.1
            width = 13
            height = 18.2
            material = cardMaterial

            translateY = -3
            transforms += new Rotate(50, Rotate.XAxis)
          }
        }
      }
      onMouseExited = _ => {
        if (zoomZone.isDefined)
          zoomZone.get.children.remove(0)
      }

      if (isHumans.isDefined && isHumans.get)
        addAction(this, cardType, cardIndex, zone)
      drawMode = DrawMode.Fill
    }
  }
}


