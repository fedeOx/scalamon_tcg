package view

import model.game.Cards.PokemonCard
import scalafx.event
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.HBox
import scalafx.stage.{Modality, Stage, Window}
import view.CardCreator.createCard

import scala.reflect.macros.whitebox

object PopupBuilder {

  def openBenchSelectionScreen(parent: Window, bench: Seq[Option[PokemonCard]], isAttackingPokemonKO: Boolean): Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(1000, 400) {
        var cardContainer = new HBox{
          prefHeight = 350
        }
        var cardList : Seq[ImageView] = Seq()
        bench.filter(c => c.isDefined).zipWithIndex.foreach{case (card,cardIndex) => {
          cardList = cardList :+ new ImageView(new Image("/assets/"+card.get.belongingSetCode+"/"+card.get.imageId+".jpg")) {
            fitWidth = 160
            fitHeight = 350
            onMouseClicked = event => {
              utils.controller.swap(cardIndex)
              scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
              if (isAttackingPokemonKO) {
                utils.controller.endTurn()
              }
            }
          }
        }}
        cardContainer.children = cardList
        content = cardContainer
      }
      sizeToScene()
      //x = parentWindow.getX + parentWindow.getWidth / 1.6
      //y = parentWindow.getY + parentWindow.getHeight / 2.8
      resizable = false
      alwaysOnTop = true
    }
    dialog.show()
  }
}
