package view

import controller.Controller
import model.game.Cards.PokemonCard
import scalafx.animation.{Interpolator, RotateTransition}
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.{MeshView, TriangleMesh}
import scalafx.scene.text.TextAlignment
import scalafx.scene.transform.Rotate
import scalafx.scene.{Node, Scene}
import scalafx.stage.{Modality, Stage, StageStyle, Window}
import scalafx.util.Duration

/** *
 * Object that creates popup messages for the game
 */
object PopupBuilder {

  private var controller: Controller = _

  def setController(c: Controller): Unit = {
    controller = c
  }

  def openLoadingScreen(parent: Window): Stage = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      initStyle(StageStyle.Undecorated)
      scene = new Scene(300, 200) {
        stylesheets = List("/style/popup.css")
        content = new VBox {
          styleClass += "message"
          children = List(new ImageView(new Image("/assets/loading.gif")) {
            fitWidth = 60
            fitHeight = 60
          }, new Label("Caricamento..."))
        }
      }
      sizeToScene()
      resizable = false
      alwaysOnTop = true
    }
    dialog
  }

  def closeLoadingScreen(loadingMessage: Stage): Unit = {
    loadingMessage.close()
  }

  def openTurnScreen(parent: Window): Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        stylesheets = List("/style/popup.css")
        content = new VBox {
          styleClass += "message"
          children = Label("Your turn")
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

  def openInvalidOperationMessage(parent: Window, message: String): Unit = {
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        stylesheets = List("/style/popup.css")
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
    val windowHeight = 600
    val windowWidth = 1300
    val dialog: Stage = new Stage() {
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      initStyle(StageStyle.Undecorated)
      initStyle(StageStyle.Transparent)
      scene = new Scene(windowWidth, windowHeight) {
        fill = Color.Transparent
        stylesheets = List("/style/popup.css")
        private val cardContainer: HBox = new HBox {

          prefWidth = windowWidth
          prefHeight = windowHeight - 100
          spacing = 20
          alignment = Pos.Center
        }
        var cardList: Seq[BorderPane] = Seq()
        bench.filter(c => c.isDefined).zipWithIndex.foreach { case (card, cardIndex) => {
          cardList = cardList :+ new BorderPane {
            center = new ImageView(new Image("/assets/" + card.get.belongingSetCode + "/" + card.get.imageId + ".jpg")) {
              fitWidth = 230
              fitHeight = 322
            }
            prefWidth = 230
            maxHeight = 322

            onMouseEntered = _ => {
              style = "-fx-border-color: red; -fx-border-style: solid; -fx-border-width: 4px;"
            }
            onMouseExited = _ => style = ""
            onMouseClicked = _ => {
              controller.swap(cardIndex)
              scene.value.getWindow.asInstanceOf[javafx.stage.Stage].close()
              if (isAttackingPokemonKO) {
                controller.endTurn()
              }
            }
          }
        }
        }
        cardContainer.children = cardList
        private val container = new VBox {
          styleClass += "benchSelection"
          prefWidth = windowWidth
          prefHeight = windowHeight
          alignment = Pos.Center
          children = List(new Label("Choose a Pokémon from bench") {
            prefHeight = 100
            prefWidth = windowWidth - 150
            textAlignment = TextAlignment.Left
          }, cardContainer)
        }
        content = container
      }
      sizeToScene()
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

  def openEndGameScreen(parent: Window, playerWon: Boolean): Unit = {
    val dialog: Stage = new Stage() {
      private val gameStage = parent
      initOwner(parent)
      initModality(Modality.ApplicationModal)
      scene = new Scene(300, 200) {
        content = new VBox() {
          prefWidth = 300
          prefHeight = 200
          alignment = Pos.Center
          children = List(new Label(if (playerWon) "You won!" else "You lose!"),
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
