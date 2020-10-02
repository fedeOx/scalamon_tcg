package view

import controller.Controller
import model.game.Cards.PokemonCard
import scalafx.animation.{Interpolator, RotateTransition}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Pos
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.{MeshView, TriangleMesh}
import scalafx.scene.text.{Text, TextAlignment}
import scalafx.scene.transform.Rotate
import scalafx.stage
import scalafx.stage.{Modality, Stage, StageStyle, Window}
import scalafx.util.Duration

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
          styleClass += "message"
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
      //initStyle(StageStyle.Undecorated)
      scene = new Scene(300, 200) {
        stylesheets = List("/style/loadingScreen.css")
        content = new VBox{
          styleClass += "message"
          children = Label("È il tuo turno")
        }
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog.show()
    Thread.sleep(1000)
    dialog.close()
  }

  def openInvalidOperationMessage(parent: Window, message: String) : Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        stylesheets = List("/style/loadingScreen.css")
        content = new VBox() {
          styleClass += "message"
          children = List(new Label(message) {
            prefWidth = 300
            maxHeight(200)
            //prefHeight = 100
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

  def openCoinFlipScreen(parent: Window, isHead: Boolean): Unit = {
    val dialog = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      initStyle(StageStyle.Undecorated)
      initStyle(StageStyle.Transparent)
      scene = new Scene(200, 200) {
        val coin = new MeshView(createCoinMesh())
        val material = new PhongMaterial()
        fill = Color.Transparent
        material.setDiffuseMap(new Image("/assets/coin.png"))
        coin.setMaterial(material)
        val rotator: RotateTransition = createRotator(coin, isHead)
        rotator.play()
        rotator.setOnFinished(e => {
          Thread.sleep(800)
          scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
        })
        val container: VBox = new VBox() {
          alignment = Pos.Center
          prefWidth = 200
          prefHeight = 200
          children = coin
        }
        content = container
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog.show()
  }

  private def createCoinMesh(): TriangleMesh = {
    val mesh = new TriangleMesh()
    mesh.getPoints.addAll(-1 * 200 / 2, -1 * 200 / 2, 0, 1 * 200 / 2, -1 * 200 / 2, 0, -1 * 200 / 2, 1 * 200 / 2, 0, 1 * 200 / 2, 1 * 200 / 2, 0)
    mesh.getFaces.addAll(0, 0, 2, 2, 3, 3, 3, 3, 1, 1, 0, 0)
    mesh.getFaces.addAll(0, 4, 3, 7, 2, 6, 3, 7, 0, 4, 1, 5)
    mesh.getTexCoords.addAll(0, 0, 0.5f, 0, 0, 1, 0.5f, 1, 0.5f, 0, 1, 0, 0.5f, 1, 1, 1)
    mesh
  }

  private def createRotator(coin: Node, isHead: Boolean) = {
    val rotator = new RotateTransition() {
      duration = Duration.apply(1500)
      node = coin
    }
    rotator.setAxis(Rotate.YAxis)
    if (isHead) rotator.setFromAngle(180) else rotator.setFromAngle(0)
    if (isHead) rotator.setToAngle(540) else rotator.setToAngle(360)
    rotator.rate = 4
    rotator.setInterpolator(Interpolator.Linear)
    rotator.setCycleCount(4)
    rotator
  }

  def openEndGameScreen(parent: Window, playerWon: Boolean) : Unit = {
    val dialog: Stage = new Stage() {
      private val gameStage = parent
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = new VBox() {
          prefWidth = 300
          prefHeight = 200
          alignment = Pos.Center
          children = List(new Label(if(playerWon)"You won!" else "You lose!"),
            new Button("Back to menu") {
              onAction = _ => {
                scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close();
                GameLauncher.stage.asInstanceOf[Stage].scene = StartGameScene()
                GameLauncher.stage.width = 500
                GameLauncher.stage.height = 300
              }
          })
        }
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog.show()
  }
}
