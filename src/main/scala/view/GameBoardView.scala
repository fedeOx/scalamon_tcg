package view

import common.TurnOwner.TurnOwner
import common.{Observer, TurnOwner}
import controller.Controller
import javafx.scene.paint.ImagePattern
import model.core.{GameManager, TurnManager}
import model.event.Events
import model.event.Events.Event
import model.event.Events.Event._
import model.ia.Ia
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{Group, PerspectiveCamera, Scene, SceneAntialiasing}
import scalafx.stage.Stage


/** *
 * Stage that contains the game scene
 */
class GameBoardView(val controller: Controller) extends JFXApp.PrimaryStage with Observer {
  //private val parentWindow : Window = this
  val zoomZone = new ZoomZone
  var turnOwner : TurnOwner = TurnOwner.Player
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"
  //private val guiEl: GuiElements = GuiElements(this, new ZoomZone)
  private val iABoard = new PlayerBoardImpl(false,this)
  private val humanBoard = new PlayerBoardImpl(true,this)
  private var loadingMessage : Stage = _

  title = TITLE
  icons += new Image("/assets/icon.png")
  controller.gameManager.addObserver(this)
  controller.turnManager.addObserver(this)
  CardCreator.setController(controller)
  PopupBuilder.setController(controller)
  x = 0
  y = 0
  scene = new Scene(WIDTH, HEIGHT, true, SceneAntialiasing.Balanced) {
    stylesheets = List("/style/PlayerBoardStyle.css")
    camera = new PerspectiveCamera(true) {
      transforms += (
        new Rotate(50, Rotate.XAxis),
        new Translate(0, 5, -75))
    }
    val playMatMaterial = new PhongMaterial()
    playMatMaterial.diffuseMap = new Image("/assets/playmat.png")
    fill = new ImagePattern(new Image("/assets/woodTable.png"))

    content = List(new Box {
      width = 55
      height = 50
      depth = 0.4
      material = playMatMaterial
    }, new Group {
      translateX = -27.5
      translateY = -25
      minWidth(55)
      minHeight(50)
      alignmentInParent = Pos.Center
      children = Seq(iABoard, humanBoard)
    }, zoomZone)
    loadingMessage = PopupBuilder.openLoadingScreen(this.window.value)
    loadingMessage.show()
  }

  resizable = true
  sizeToScene()
  show()

  onCloseRequest = _ => controller.interruptAi()

  override def update(event: Events.Event): Unit = event match {
    case event : BuildGameField => initializeBoards(event)
    case event : FlipCoin => flipCoin(event)
    case _ : UpdateBoards => updateBoards()
    case event : NextTurn => handleTurnStart(event)
    case event : PokemonKO => handleKO(event)
    case _ : AttackEnded => handleAttackEnd()
    case _ : EndGame => endGame()
    case _ =>
  }

  private def initializeBoards(event: BuildGameField): Unit = {
    humanBoard.myBoard = event.playerBoard
    humanBoard.opponentBoard = event.opponentBoard
    iABoard.myBoard = event.opponentBoard
    iABoard.opponentBoard = event.playerBoard
    Platform.runLater({
      humanBoard.updateHand()
      humanBoard.updatePrizes()
      humanBoard.updateActive()
      iABoard.updatePrizes()
    })
    Platform.runLater(PopupBuilder.closeLoadingScreen(loadingMessage))
  }

  private def flipCoin(event: FlipCoin): Unit = {
    Platform.runLater(PopupBuilder.openCoinFlipScreen(this, event.isHead))
  }

  private def updateBoards() : Unit = {
    Platform.runLater({
      humanBoard.updateActive()
      humanBoard.updateHand()
      humanBoard.updateBench()
      humanBoard.updateDiscardStack()
      if (!humanBoard.isFirstTurn) {
        Platform.runLater({
          iABoard.updateBench()
          iABoard.updateActive()
          iABoard.updateDiscardStack()
        })
      }
    })
  }

  private def handleTurnStart(event: NextTurn) : Unit = {
    turnOwner = event.turnOwner
    if(event.turnOwner == TurnOwner.Player) {
      humanBoard.alterButton(false)
      controller.activePokemonStatusCheck()
      Platform.runLater({
        PopupBuilder.openTurnScreen(this)
        controller.drawCard()
        humanBoard.updateHand()
      })
    } else
      humanBoard.alterButton(true)
    Platform.runLater({
      iABoard.updateBench()
      iABoard.updateActive()
      iABoard.updateDiscardStack()
    })
  }

  private def handleKO(event: PokemonKO): Unit = {
    //TODO: controllo sulla board
    if(event.board.eq(humanBoard.myBoard)){
      if (humanBoard.myBoard.activePokemon.get.isKO && humanBoard.myBoard.pokemonBench.exists(card => card.isDefined)
        && iABoard.myBoard.prizeCards.size > 1)
        Platform.runLater(PopupBuilder.openBenchSelectionScreen(this,
          humanBoard.myBoard.pokemonBench, event.isPokemonInCharge))
    }
    Platform.runLater({
      humanBoard.updateActive()
      humanBoard.updatePrizes()
      iABoard.updatePrizes()
      iABoard.updateActive()
    })
  }

  private def handleAttackEnd() : Unit = {
    if(turnOwner.equals(TurnOwner.Player) && !humanBoard.myBoard.activePokemon.get.isKO) {
      Platform.runLater(controller.endTurn())
    }
  }

  private def endGame() : Unit = {
    println("fermo il gioco")
    val playerBoard = humanBoard.myBoard
    val opponentBoard = humanBoard.opponentBoard
    var playerWon: Boolean = false
    if(playerBoard.prizeCards.isEmpty ||
      (opponentBoard.activePokemon.get.isKO && !opponentBoard.pokemonBench.exists(card => card.isDefined)))
      playerWon = true
    else if (opponentBoard.prizeCards.isEmpty)
      playerWon = false
    else
      playerWon = false
    Platform.runLater(PopupBuilder.openEndGameScreen(this, playerWon))
    controller.interruptAi()
  }
}

