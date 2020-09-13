package view

import scalafx.geometry.Pos
import scalafx.scene.layout.HBox
import scalafx.scene.shape.Box
import view.CardCreator.createCard

import scala.collection.mutable

/***
 * The field zone that contains the benched pokemon
 * @param zone: the zone for the zoomed cards
 * @param isHumans: true if it's the human's board
 */
case class BenchZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  private val WIDTH = 35
  private val HEIGHT = 10
  private var bench : mutable.Seq[Box] = mutable.Seq()
  updateView()

  def updateView(): Unit = {
    for (cardIndex <- 0 to 4) {
      bench = bench :+ createCard("/assets/1.jpg", Some(zone), CardType.bench, cardIndex = cardIndex, isHumans = Some(isHumans))
    }
    children = bench
  }

  spacing = 0.5
  prefWidth = WIDTH
  prefHeight = HEIGHT
  alignment = Pos.Center
  translateX = 10
  translateY = 15
  onMouseClicked = _ => println("benchZone")
}
