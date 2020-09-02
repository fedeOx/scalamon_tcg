package view

import javafx.geometry
import scalafx.Includes.when
import scalafx.geometry.Insets
import scalafx.scene.image.{Image, ImageView}

class CardComponent(imgUrl : String, imgWidth: Double, imgHeight: Double,
                    isHumans: Boolean, canScale: Boolean) {
  val card: ImageView = new ImageView(new Image(imgUrl)) {
     if (canScale)
       fitWidth <== when(hover) choose 357 otherwise imgWidth
     else
       fitWidth = imgWidth
    if (canScale)
      fitHeight <== when(hover) choose 500 otherwise imgHeight
    else
      fitHeight = imgHeight
    if (!isHumans) rotate = 180
    onMouseClicked = _ => println("ciao")
    margin = new Insets(new geometry.Insets(0,20,0,0))
  }

}
