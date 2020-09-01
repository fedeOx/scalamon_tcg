package view

import javafx.geometry
import scalafx.Includes.when
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Border, BorderStroke, GridPane, HBox, VBox}
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color._

import scala.collection.mutable

/***
 * Player board component for the human player
 */
class HumanPlayerBoard extends PlayerBoard {
  styleClass += "humanPB"
  private val gameGrid = new GridPane() {
    add(new PrizeCardsZone,0,0,1,2)
    add(new ActivePkmnZone,1,0,1,1)
    add(new BenchZone,1,1,1,1)
    add(new DeckDiscardZone,2,0,1,2)
  }
  children = gameGrid
}

class PrizeCardsZone extends VBox {
  children = List(new ImageView(new Image("/assets/cardBack.jpg")) {
    fitWidth = 150
    fitHeight = 210
  }, new Label("Remaining: 6"))
  alignment = Pos.Center
  minWidth = 200
  styleClass += "prizeCards"
}

class DeckDiscardZone extends HBox {
  private val discardStack = mutable.Stack(new Image("/assets/1.jpg"), new Image("/assets/4.jpg"))
  children = List(new ImageView(new Image("/assets/cardBack.jpg")) {
    fitWidth = 150
    fitHeight = 210
    margin = new Insets(new geometry.Insets(0,20,0,0))
  }, new ImageView(discardStack.top) {
    fitWidth = 150
    fitHeight = 210
  })

  alignment = Pos.Center
  minWidth = 400
  styleClass += "deckDiscard"
}

class ActivePkmnZone extends HBox {
  minWidth = 1000
  minHeight = 250
  alignment = Pos.Center
  children = new ImageView(new Image("/assets/4.jpg")) {
    fitWidth <== when(hover) choose 357 otherwise 150
    fitHeight <== when(hover) choose 500 otherwise 210

  }
}

class BenchZone extends HBox {
  private val bench = List(new ImageView(new Image("/assets/1.jpg")) {
    fitWidth <== when(hover) choose 357 otherwise 110
    fitHeight <== when(hover) choose 500 otherwise 154
    margin = new Insets(new geometry.Insets(0,20,0,0))
  } , new ImageView(new Image("/assets/4.jpg")) {
    fitWidth <== when(hover) choose 357 otherwise 110
    fitHeight <== when(hover) choose 500 otherwise 154
    y <== when(hover) choose 300 otherwise 710
    margin = new Insets(new geometry.Insets(0,20,0,0))
  })
  children = bench

  minWidth = 800
  minHeight = 200
  alignment = Pos.BottomLeft
}
