package view.game

import common.TurnOwner
import model.game.Board
import scalafx.Includes._
import scalafx.scene.Group
import scalafx.scene.image.Image
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import scalafx.stage.Window

/**
 * The game board relative to a player
 */
trait PlayerBoard extends Group {
  /**
   * The PlayerBoard's owner Board
   */
  var myBoard : Board

  /**
   * The PlayerBoard's opponent's Board
   */
  var opponentBoard : Board

  /**
   * The scalaFx Window where the game components are visualized
   * @return the ScalaFx parent Window
   */
  def gameWindow: Window

  /**
   * True if it's the first turn
   * @return the value of isFirstTurn
   */
  def isFirstTurn : Boolean

  /**
   * Updates the PlayerBoard's owner HandZone
   */
  def updateHand(): Unit

  /**
   * Updates the PlayerBoard's owner ActivePkmnZone
   */
  def updateActive() : Unit

  /**
   * Updates the PlayerBoard's owner Bench
   */
  def updateBench() : Unit

  /**
   * Updates the PlayerBoard's owner PrizeCardsZone
   */
  def updatePrizes() : Unit

  /**
   * Updates the PlayerBoard's owner DeckDiscardZone
   */
  def updateDeckAndDiscardStack(): Unit

  /**
   * Edit the appearance of the endTurnButton
   * @param isDisabled: true if the button has to appear disabled
   */
  def turnStart(isDisabled: Boolean): Unit
}

object PlayerBoard {
  /**
   * Creates a PlayerBoard
   * @param isHumans: true if it's the human PlayerBoard
   * @param parentWindow: the ScalaFx parent Window
   * @return the PlayerBoard instance
   */
  def apply(isHumans: Boolean, parentWindow: Window): PlayerBoard = PlayerBoardImpl(isHumans, parentWindow)

  private case class PlayerBoardImpl(isHumans: Boolean, parentWindow: Window) extends PlayerBoard {
    var myBoard : Board = _
    var opponentBoard : Board = _
    val gameWindow : Window = parentWindow
    var isFirstTurn : Boolean = true
    private val WIDTH = 55
    private val HEIGHT = 25
    private val prize = PrizeCardsZone(isHumans, this)
    private val active = ActivePkmnZone(isHumans, this)
    private val bench = BenchZone(isHumans, this)
    private val deckDiscard = DeckDiscardZone()
    private var hand : HandZone = _
    private var endTurnButton: Box = _

    children = List(prize, active, bench, deckDiscard)
    if (isHumans) {
      hand = HandZone(isHumans, this)
      children += hand
      endTurnButton = new Box{
        var buttonMaterial = new PhongMaterial()
        buttonMaterial.diffuseMap = new Image("/assets/endturn.png")
        material = buttonMaterial
        width = 3
        height = 3
        translateX = 42
        translateY = 5
        depth = 0.6

        onMouseClicked = _ => {
          if(isFirstTurn ) {
            if (myBoard.activePokemon.isDefined) {
              isFirstTurn = false
              gameWindow.asInstanceOf[GameBoardView].controller.playerReady()
            }
          } else {
            if (gameWindow.asInstanceOf[GameBoardView].turnOwner == TurnOwner.Player) {
              gameWindow.asInstanceOf[GameBoardView].controller.endTurn()
            }
          }
        }
      }
      children += endTurnButton
    }

    minWidth(WIDTH)
    minHeight(HEIGHT)
    maxWidth(WIDTH)
    maxHeight(HEIGHT)

    if (!isHumans) rotate = 180 else translateY = 25

    override def updateHand() : Unit = hand.updateView(myBoard.hand)
    override def updateActive() : Unit = active.updateView(myBoard.activePokemon)
    override def updateBench() : Unit = bench.updateView(myBoard.pokemonBench)
    override def updatePrizes() : Unit = prize.updateView(myBoard.prizeCards)
    override def updateDeckAndDiscardStack() : Unit = deckDiscard.updateView(myBoard.deck, myBoard.discardStack)
    override def turnStart(isDisabled: Boolean): Unit = {
      if(isDisabled)
        endTurnButton.material.value.asInstanceOf[javafx.scene.paint.PhongMaterial].diffuseColor = Color.DarkGray
      else {
        endTurnButton.material.value.asInstanceOf[javafx.scene.paint.PhongMaterial].diffuseColor = Color.White
        active.resetRetreat()
      }
    }
  }
}