package view

import scalafx.geometry
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.chart.LineChart.SortingPolicy.XAxis
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Background, BackgroundFill, BorderPane, GridPane, HBox, VBox}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.{Box, CullFace, DrawMode}
import scalafx.scene.transform.Rotate
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.print.PrintColor
import scalafx.print.PrintColor.Color
import scalafx.scene._
import scalafx.scene.image.Image
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BackgroundFill, GridPane, HBox}
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.{Box, DrawMode}
import scalafx.scene.transform.Transform._
import scalafx.scene.transform.{Rotate, Translate}

import scala.collection.mutable

/***
 * Player board component for the human player
 */
class HumanPlayerBoard(isHumans: Boolean) extends GridPane {
  styleClass += "humanPB"

    val zoomZone = new ZoomZone

    add(new PrizeCardsZone,0,0,1,2)
    add(new ActivePkmnZone(zoomZone, isHumans),1,0,1,1)
    add(new BenchZone(zoomZone, isHumans),1,1,1,1)
    add(new DeckDiscardZone,2,0,1,2)
    /*add(new Box() {
      onMouseEntered = _ => println("sono un box")
    },0,0,3,2)*/
  //if (isHumans)
       //add(new HandZone(zoomZone),0,2,3,2)
    //add(zoomZone,2,0,1,2)

  minWidth = 55
  minHeight = 25

  maxWidth = 55
  maxHeight = 25

  if(!isHumans)
    rotate = 180

  onMouseEntered = _ => println("sono la griglia")
}

class PrizeCardsZone extends VBox {
  children = List(new CardComponent("/assets/cardBack.jpg",150,210,true,false).card)
  alignment = Pos.Center
  //minWidth = 300
  minHeight = 25
  maxHeight = 25
  minWidth = 10
  maxWidth = 10
  styleClass += "prizeCards"
  onMouseEntered = _ => println("sono la zona carte premio")
}

class DeckDiscardZone extends HBox {
  private val discardStack = mutable.Stack("/assets/1.jpg", "/assets/4.jpg")
  children = List(new CardComponent("/assets/cardBack.jpg",150,210,true,false).card,
    new CardComponent(discardStack.top,150,210,true,false).card)


  alignment = Pos.Center
  maxWidth = 10
  maxHeight = 25
  styleClass += "deckDiscard"
}

class ActivePkmnZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  maxWidth = 35
  maxHeight = 15
  //minHeight = 250
  alignment = Pos.BottomCenter

  children = new CardComponent("/assets/4.jpg",150,210,isHumans,true,Some(zone)).card

}

class BenchZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  private val bench = List(new CardComponent("/assets/1.jpg",110,154,isHumans,true,Some(zone)).card,
    new CardComponent("/assets/4.jpg",110,154,isHumans,true,Some(zone)).card)

  children = bench

  //margin = new Insets(new geometry.Insets(0,0,0,0))

  minWidth = 35
  maxWidth = 35
  minHeight = 10
  maxHeight = 10
  //minHeight = 160
  alignment = Pos.BottomLeft

}

class HandZone(zone: ZoomZone) extends HBox {
    private val hand = List(new CardComponent("/assets/4.jpg",110,154,true,true,Some(zone)).card,
      new CardComponent("/assets/1.jpg",110,154,true,true,Some(zone)).card)
    children = hand

    //margin = new Insets(new geometry.Insets(0,0,0,0))
    //minWidth = 1600
    //minHeight = 160
    alignment = Pos.BottomLeft
}

class ZoomZone extends HBox {
  alignment = Pos.TopLeft

}

object cardCreator {
  def cardPosition(cardImage: Image, yTransl: Double = 30, zTransl: Double = -10, rotateX: Double = 0): Box = {
    val cardMaterial = new PhongMaterial()
    cardMaterial.diffuseMap = cardImage
    new Box {
      width = 6
      height = 8.40
      depth = 0.5

      material = cardMaterial
      onMouseClicked = _ => {
        width = 12
        height = 16.80
        translateZ = -5
        //transforms += Seq(new Rotate(45, Rotate.XAxis))
        transforms += new Rotate(45, Rotate.XAxis)
      }
      /*onMouseEntered = _ => {
        width = 12
        height = 16.80
        translateZ = -5
        //transforms += Seq(new Rotate(45, Rotate.XAxis))
        transforms += new Rotate(45, Rotate.XAxis)
      }*/
      onMouseExited = _ => {
        width = 6
        height = 8.40
        translateZ = 0
        transforms += new Rotate(-45, Rotate.XAxis)
        //transforms = Seq(new Rotate(-45, Rotate.XAxis))
      }
      drawMode = DrawMode.Fill

    }
  }
}
