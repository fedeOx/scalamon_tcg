package view

import scalafx.Includes._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.{Box, DrawMode}
import scalafx.scene.text.Text
import scalafx.scene.transform.Rotate
import scalafx.scene.transform.Transform._

/***
 * Class that represents the card graphic component
 * @param imgUrl : the url of the image
 * @param zoomZone : the zone for the zoomed cards
 * @param transX : the x translation to applicate for the cards in hand
 * @param isActive : true if the card represents an active pokemon
 */
class CardComponent(imgUrl: String, zoomZone: Option[ZoomZone] = None, transX: Double = 0, isActive: Boolean = false) {

  val card: Box = cardCreator.cardPosition(new Image(imgUrl), zoomZone)

  object cardCreator {
    def cardPosition(cardImage: Image, zoomZone: Option[ZoomZone]): Box = {
      val cardMaterial = new PhongMaterial()
      cardMaterial.diffuseMap = cardImage
      new Box {
        if (isActive) width = 8 else width = 5.5 //6
        if(isActive) height =  11.2 else height = 7.7 //8.4
        depth = 0.5

        translateX = -transX
        material = cardMaterial

        onMouseEntered = _ => {
          if (zoomZone.isDefined) {
            /*zoomZone.get.children = new StackPane{
              children = Seq(new ImageView(new Image(imgUrl)){
                fitWidth = 13
                fitHeight = 18.2
                translateY = 0
                translateX = 0
                translateZ = -10
                transforms += new Rotate(50, Rotate.XAxis)
              }, new Text("40HP") {
                //prefHeight =  2
                //prefWidth = 4
                //translateZ = -12
                style = "-fx-font: 2 arial;"
                transforms += new Rotate(50, Rotate.XAxis)
              })
            }*/
            zoomZone.get.children = new Box {
              depth = 0.1
              width = 13
              height = 18.2
              material = cardMaterial

              translateY = -3
              translateZ = -4
              transforms += new Rotate(50, Rotate.XAxis)
            }
          }
        }
        onMouseExited = _ => {
          if (zoomZone.isDefined)
            zoomZone.get.children.remove(0)
        }
        drawMode = DrawMode.Fill

      }
    }
  }


}


