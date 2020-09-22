package view

import model.game.Cards.PokemonCard
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.HBox
import scalafx.stage.{Modality, Stage, StageStyle, Window}

/***
 * Object that creates popup messages for the game
 */
object PopupBuilder {

  def openLoadingScreen(parent: Window) : Stage = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = new Label("Caricamento...")
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog
  }

  def closeLoadingScreen(loadingMessage: Stage) : Unit = {
    loadingMessage.close()
  }

  def openInvalidOperationMessage(parent: Window, message: String) : Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = List(new Label(message), new Button("Ok") {
          onAction = _ => scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
        })
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog.showAndWait()
  }

  def openBenchSelectionScreen(parent: Window, bench: Seq[Option[PokemonCard]], isAttackingPokemonKO: Boolean): Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      initStyle(StageStyle.Undecorated)
      scene = new Scene(1000, 400) {
        private val cardContainer: HBox = new HBox{
          prefWidth = 1000
          prefHeight = 350
          spacing = 10
          alignment = Pos.Center
        }
        var cardList : Seq[ImageView] = Seq()
        bench.filter(c => c.isDefined).zipWithIndex.foreach{case (card,cardIndex) => {
          cardList = cardList :+ new ImageView(new Image("/assets/"+card.get.belongingSetCode+"/"+card.get.imageId+".jpg")) {
            fitWidth = 180
            fitHeight = 280
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
