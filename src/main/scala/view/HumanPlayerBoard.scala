package view

import scalafx.Includes.when
import scalafx.geometry.Pos
import scalafx.scene.layout.{Border, BorderStroke, GridPane, VBox}
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color._

class HumanPlayerBoard extends PlayerBoard {
  stylesheets = List("/style/PlayerBoardStyle.css")
  styleClass += "body"
  val gameGrid = new GridPane() {
    /*add(new Rectangle{
      width = 200
      height = 450
      //fill <== when(hover) choose Green otherwise Transparent
      styleClass += "prizeCards"
    },0,0,1,2)*/
    add(new VBox(){
      children = new Rectangle{
        width = 150
        height = 300
      }
      alignment = Pos.Center
      minWidth = 200
      styleClass += "prizeCards"
    },0,0,1,2)
    add(new Rectangle{
      width = 1200
      height = 250
      //fill = Transparent
      fill <== when(hover) choose Blue otherwise Transparent
    },1,0,1,1)
    add(new Rectangle{
      width = 1200
      height = 200
      fill <== when(hover) choose Red otherwise Transparent
    },1,1,1,1)
    add(new Rectangle{
      width = 200
      height = 450
      fill <== when(hover) choose Green otherwise Transparent
    },2,0,1,2)
  }
  children = gameGrid
}
