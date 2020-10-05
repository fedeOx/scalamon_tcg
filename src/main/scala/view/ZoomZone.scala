package view

import javafx.geometry.Insets
import model.game.Cards.{Card, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.StatusType
import model.game.StatusType.StatusType
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.Rotate

/**
 * Zone of the fields where the hovered card's information's are shown
 */
trait ZoomZone extends HBox {
  /**
   * Shows the card's image and informations in the ZoomZone
   * @param card: the card to visualize
   */
  def showContent(card: Card): Unit
}

object ZoomZone {
  /**
   * Creates the ZoomZone
   * @return the ZoomZone instance
   */
  def apply(): ZoomZone = ZoomZoneImpl()

  private case class ZoomZoneImpl() extends ZoomZone {
    maxWidth = 13
    maxHeight = 18.2
    translateX = -27
    translateY = 0
    translateZ = -9
    alignment = Pos.TopLeft

    def showContent(card: Card): Unit = {
      val cardMaterial = new PhongMaterial()
      cardMaterial.diffuseMap = new Image(new Image("/assets/"+card.belongingSetCode+"/"+card.imageId+".png"))
      children = Seq(new Box {
        depth = 0.1
        width = 13
        height = 18.2
        material = cardMaterial
        translateY = -3
        transforms += new Rotate(50, Rotate.XAxis)
      })
      card match {
        case pokemonCard: PokemonCard => children += visualizePokemonInfo(pokemonCard)
        case _ =>
      }
    }

    private def visualizePokemonInfo(card: PokemonCard): VBox = {
      val infoBox : VBox = new VBox()
      infoBox.alignment = Pos.TopCenter
      infoBox.translateY = 5
      infoBox.translateX = 1.5
      infoBox.spacing = 0
      infoBox.translateZ = -9
      infoBox.transforms += new Rotate(50, Rotate.XAxis)
      if (card.initialHp != card.actualHp)
        infoBox.children += createInfoBox(card.initialHp - card.actualHp)
      if (!card.status.equals(StatusType.NoStatus))
        infoBox.children += createInfoBox(card.status)
      card.energiesMap.foreach(energy => {
        for (_ <- 0 until energy._2)
          infoBox.children += createInfoBox(energy._1)
      })
      infoBox
    }

    private def createInfoBox(args: Any): Box = args match {
      case damage : Int => generateBox(new Image("/assets/dmg/"+ damage +".png"))
      case status : StatusType => generateBox(new Image("/assets/status/"+status+".png"))
      case energy : EnergyType => generateBox(new Image("/assets/energy/"+energy+".png"))
    }

    private def generateBox(image: Image) : Box = {
      new Box {
        depth = 0.1
        width = 1.2
        height = 1.2
        val infoMaterial = new PhongMaterial()
        infoMaterial.diffuseMap = image
        material = infoMaterial
        margin = new Insets(0,0,-0.55,0)
      }
    }
  }
}