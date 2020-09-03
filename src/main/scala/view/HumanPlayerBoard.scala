package view

import javafx.geometry
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{GridPane, HBox, VBox}

import scala.collection.mutable

/***
 * Player board component for the human player
 */
class HumanPlayerBoard(isHumans: Boolean) extends PlayerBoard {
  styleClass += "humanPB"
  private val gameGrid = new GridPane() {
    val zoomZone = new ZoomZone

    add(new PrizeCardsZone,0,0,1,2)
    add(new ActivePkmnZone(zoomZone, isHumans),1,0,1,1)
    add(new BenchZone(zoomZone, isHumans),1,1,1,1)
    add(new DeckDiscardZone,2,0,1,2)
     if (isHumans)
       add(new HandZone(zoomZone),0,2,3,2)
    add(zoomZone,2,0,1,2)



  }
  if(!isHumans)
    rotate = 180
  children = gameGrid
}

class PrizeCardsZone extends VBox {
  children = List(new CardComponent("/assets/cardBack.jpg",150,210,true,false).card
    , new Label("Remaining: 6"))
  alignment = Pos.Center
  minWidth = 300
  styleClass += "prizeCards"
}

class DeckDiscardZone extends HBox {
  private val discardStack = mutable.Stack("/assets/1.jpg", "/assets/4.jpg")
  children = List(new CardComponent("/assets/cardBack.jpg",150,210,true,false).card,
    new CardComponent(discardStack.top,150,210,true,false).card)


  alignment = Pos.Center
  minWidth = 400
  styleClass += "deckDiscard"
}

class ActivePkmnZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  minWidth = 900
  minHeight = 250
  alignment = Pos.BottomCenter

  children = new CardComponent("/assets/4.jpg",150,210,isHumans,true,Some(zone)).card

}

class BenchZone(zone: ZoomZone, isHumans: Boolean) extends HBox {
  private val bench = List(new CardComponent("/assets/1.jpg",110,154,isHumans,true,Some(zone)).card,
    new CardComponent("/assets/4.jpg",110,154,isHumans,true,Some(zone)).card)

  children = bench

  margin = new Insets(new geometry.Insets(0,10,0,100))
  minWidth = 800
  minHeight = 160
  alignment = Pos.BottomLeft
}

class HandZone(zone: ZoomZone) extends HBox {
    private val hand = List(new CardComponent("/assets/4.jpg",110,154,true,true,Some(zone)).card,
      new CardComponent("/assets/1.jpg",110,154,true,true,Some(zone)).card)
    children = hand

    margin = new Insets(new geometry.Insets(0,100,0,100))
    minWidth = 1600
    minHeight = 160
    alignment = Pos.BottomLeft
}

class ZoomZone extends HBox {
  alignment = Pos.TopLeft

}
