package view.game

import common.TurnOwner.TurnOwner
import common.{CoinUtil, Events, Observer, TurnOwner}
import controller.Controller
import javafx.scene.paint.ImagePattern
import common.Events._
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{Group, PerspectiveCamera, Scene, SceneAntialiasing}
import scalafx.stage.Stage
import view.PopupBuilder

/** *
 * Stage that contains the game scene
 */
class GameBoardView(val controller: Controller) extends JFXApp.PrimaryStage with Observer {
  val zoomZone: CardDetailZone = CardDetailZone()
  var turnOwner: TurnOwner = TurnOwner.Player
  var isHandlingEffect: Boolean = false
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"
  private val aIBoard = PlayerBoard(isHumans = false, this)
  private val humanBoard = PlayerBoard(isHumans = true, this)
  private var loadingMessage: Stage = _
  private var gameEnded: Boolean = false
  title = TITLE
  icons += new Image("/assets/icon.png")
  controller.gameManager.addObserver(this)
  controller.turnManager.addObserver(this)
  CoinUtil.addObserver(this)
  CardFactory.setController(controller)
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
      children = Seq(aIBoard, humanBoard)
    }, zoomZone)
    loadingMessage = PopupBuilder.openLoadingScreen(this.window.value)
    loadingMessage.show()
  }

  resizable = true
  maximized = true
  show()
  onCloseRequest = _ => controller.resetGame()

  override def update(event: Events.Event): Unit = event match {
    case event: BuildGameFieldEvent => initializeBoards(event)
    case event: FlipCoinEvent => flipCoin(event)
    case _: UpdateBoardsEvent => updateBoards()
    case event: NextTurnEvent => handleTurnStart(event)
    case event: DamageBenchEvent => damageBench(event)
    case event: PokemonKOEvent => handleKO(event)
    case _: EndTurnEvent => handleAttackEnd()
    case _: EndGameEvent => endGame()
    case _ =>
  }

  private def initializeBoards(event: BuildGameFieldEvent): Unit = {
    humanBoard.myBoard = event.playerBoard
    humanBoard.opponentBoard = event.opponentBoard
    aIBoard.myBoard = event.opponentBoard
    aIBoard.opponentBoard = event.playerBoard
    Platform.runLater({
      humanBoard.updateHand()
      humanBoard.updatePrizes()
      humanBoard.updateActive()
      aIBoard.updatePrizes()
    })
    Platform.runLater(PopupBuilder.closeLoadingScreen(loadingMessage))
  }

  private def flipCoin(event: FlipCoinEvent): Unit = {
    Platform.runLater(PopupBuilder.openCoinFlipScreen(this, event.isHead))
  }

  private def updateBoards(): Unit = {
    Platform.runLater({
      humanBoard.updateActive()
      humanBoard.updateHand()
      humanBoard.updateBench()
      humanBoard.updateDeckAndDiscardStack()
      humanBoard.updatePrizes()
      if (!humanBoard.isFirstTurn) {
        Platform.runLater({
          aIBoard.updateBench()
          aIBoard.updateActive()
          aIBoard.updateDeckAndDiscardStack()
          aIBoard.updatePrizes()
        })
      }
    })
  }

  private def handleTurnStart(event: NextTurnEvent): Unit = {
    Platform.runLater(humanBoard.updateDeckAndDiscardStack())
    if (!gameEnded) {
      turnOwner = event.turnOwner
      if (event.turnOwner == TurnOwner.Player) {
        humanBoard.turnStart(false)
        controller.activePokemonStatusCheck()
        Platform.runLater({
          PopupBuilder.openTurnScreen(this)
          controller.drawCard()
          humanBoard.updateHand()
        })
      } else
        humanBoard.turnStart(true)
      Platform.runLater({
        aIBoard.updateBench()
        aIBoard.updateActive()
        aIBoard.updateDeckAndDiscardStack()
      })
    }
  }

  def damageBench(event: DamageBenchEvent): Unit = {
    isHandlingEffect = true
    Platform.runLater({
      PopupBuilder.openDamageBenchedPokemonScreen(this, humanBoard.myBoard,
        aIBoard.myBoard, event.pokemonToDamage, event.damage)
    })
  }

  private def handleKO(event: PokemonKOEvent): Unit = {
    if (event.board.eq(humanBoard.myBoard) && humanBoard.myBoard.activePokemon.get.isKO && humanBoard.myBoard.pokemonBench.exists(card => card.isDefined)
      && aIBoard.myBoard.prizeCards.size > 1)
      Platform.runLater(PopupBuilder.openBenchSelectionScreen(this,
        humanBoard.myBoard.pokemonBench, event.isPokemonInCharge))
    Platform.runLater({
      humanBoard.updateActive()
      humanBoard.updatePrizes()
      aIBoard.updatePrizes()
      aIBoard.updateActive()
    })
  }

  private def handleAttackEnd(): Unit = {
    if (turnOwner.equals(TurnOwner.Player) && !humanBoard.myBoard.activePokemon.get.isKO
      && !isHandlingEffect) {
      Platform.runLater(controller.endTurn())
    }
  }

  private def endGame(): Unit = {
    val playerBoard = humanBoard.myBoard
    val opponentBoard = humanBoard.opponentBoard
    var playerWon: Boolean = false
    gameEnded = true
    if (playerBoard.prizeCards.isEmpty ||
      (opponentBoard.activePokemon.get.isKO && !opponentBoard.pokemonBench.exists(card => card.isDefined)))
      playerWon = true
    else if (opponentBoard.prizeCards.isEmpty)
      playerWon = false
    else
      playerWon = false
    Platform.runLater(PopupBuilder.openEndGameScreen(this, playerWon))
    controller.resetGame()
  }
}

