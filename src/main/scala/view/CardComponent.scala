package view

import javafx.geometry
import scalafx.geometry.Insets
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.{Box, DrawMode}
import scalafx.scene.transform.{Rotate, Translate}



class CardComponent(imgUrl: String, imgWidth: Double, imgHeight: Double,
                    isHumans: Boolean, canScale: Boolean, zoomZone: Option[ZoomZone] = None) {
  val cardImgView: ImageView = new ImageView(new Image(imgUrl)) {
    fitWidth = imgWidth
    fitHeight = imgHeight
    if (zoomZone.isDefined) {
      onMouseEntered = _ => zoomZone.get.children = new ImageView(new Image(imgUrl)) {
        fitWidth = 357
        fitHeight = 500
        if(!isHumans)
          rotate = 180
      }
      onMouseExited = _ => zoomZone.get.children.remove(0)
    }

    //if (!isHumans) rotate = 180
    styleClass += "card"
    onMouseClicked = _ => println("ciao")
    margin = new Insets(new geometry.Insets(0, 20, 0, 0))
  }

  val card = cardCreator.cardPosition(new Image(imgUrl))



}


