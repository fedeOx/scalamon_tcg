package view

import controller.Controller
import model.game.Cards.PokemonCard
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Text, TextAlignment}
import scalafx.stage.{Modality, Stage, StageStyle, Window}

/***
 * Object that creates popup messages for the game
 */
object PopupBuilder {

  private var controller: Controller = _

  def setController(c: Controller): Unit = {
    controller = c
  }

  def openLoadingScreen(parent: Window) : Stage = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      initStyle(StageStyle.Undecorated)
      scene = new Scene(300, 200) {
        stylesheets = List("/style/loadingScreen.css")
        content = new VBox {
          prefWidth = 300
          prefHeight = 200
          alignment = Pos.Center
          spacing = 20
          children = List(new ImageView(new Image("/assets/loading.gif")) {
            fitWidth = 60
            fitHeight = 60
          },new Label("Caricamento..."))
        }
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

  def openTurnScreen(parent: Window) : Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = new Label("È il tuo turno")
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog.show()
    Thread.sleep(1000)
    println("chiuso popup")
    dialog.close()
  }

  def openInvalidOperationMessage(parent: Window, message: String) : Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = new VBox() {
          prefWidth = 300
          prefHeight = 200
          alignment = Pos.Center
          spacing = 10
          children = List(new Label(message) {
            prefWidth = 300
            maxHeight(200)
            prefHeight = 50
            wrapText = true
            textAlignment = TextAlignment.Center
          }, new Button("Ok") {
            onAction = _ => scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
          })
        }
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
              controller.swap(cardIndex)
              scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
              if (isAttackingPokemonKO) {
                controller.endTurn()
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
