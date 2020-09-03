package view

import scalafx.scene.layout.GridPane

class IAPlayerBoard extends PlayerBoard {
  styleClass += "humanPB"
  private val gameGrid = new GridPane() {
    add(new PrizeCardsZone,2,0,1,2)
    add(new BenchZone,1,0,1,1)
    add(new ActivePkmnZone,1,1,1,1)
    add(new DeckDiscardZone,0,0,1,2)
  }
  children = gameGrid
}
