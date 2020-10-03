package view

import model.game.Cards.PokemonCard
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import view.CardCreator.createCard

/***
 * The field zone that contains the benched pokemon
 */
trait BenchZone extends HBox {
  /**
   * True if a card was clicked
   */
  var cardClicked: Boolean

  /**
   * Updates the HBox children
   * @param cards: the cards that are located in the bench
   */
  def updateView(cards: Seq[Option[PokemonCard]] = Seq()): Unit
}

object BenchZone {
  /**
   * creates an instance of BenchZone
   * @param isHumans: true if it's the human's BenchZone
   * @param board: the parent PlayerBoard
   * @return an instance of BenchZone
   */
  def apply(isHumans: Boolean, board: PlayerBoard): BenchZone = BenchZoneImpl(isHumans, board)

  private case class BenchZoneImpl(isHumans: Boolean, board: PlayerBoard) extends BenchZone {
    private val WIDTH = 35
    private val HEIGHT = 10
    private var bench : Seq[Box] = Seq()
    private var isEmpty : Boolean = _
    private val parentBoard = board
    var cardClicked = false

    updateView()
    def updateView(cards: Seq[Option[PokemonCard]] = Seq()): Unit = {
      if (cards.isEmpty || (cards.count(c => c.isEmpty) == 5)) {
        isEmpty = true
        val cardMaterial = new PhongMaterial()
        cardMaterial.diffuseColor = Color.Transparent
        bench = Seq(new Box {
          material = cardMaterial
          depth = 0.5
        })
      } else {
        isEmpty = false
        bench = Seq[Box]()
        cards.filter(c => c.isDefined).zipWithIndex.foreach{case (card,cardIndex) => {
          bench = bench :+ createCard("/assets/"+card.get.belongingSetCode+"/"+card.get.imageId+".jpg",
            Some(board.gameWindow.asInstanceOf[GameBoardView].zoomZone), CardType.Bench, cardIndex = cardIndex,
            isHumans = Some(isHumans), zone = Some(this), board = Some(parentBoard.myBoard),
            gameWindow = Some(board.gameWindow))
        }}
      }
      children = bench
    }

    spacing = 0.5
    minWidth = WIDTH
    minHeight = HEIGHT
    prefWidth = WIDTH
    prefHeight = HEIGHT
    alignment = Pos.Center
    translateX = 10
    translateY = 15
    onMouseClicked = _ => {
      if (isHumans && !bench.size.equals(5)) {
        if (cardClicked)
          cardClicked = false
        else {
          //TODO: modifica
          if (isEmpty) {
            try {
              board.gameWindow.asInstanceOf[GameBoardView].controller.selectBenchLocation(0)
            } catch {
              case ex: Exception => PopupBuilder.openInvalidOperationMessage(board.gameWindow,ex.getMessage)
            }
            println("panchina vuota")
          }
          else {
            try {
              board.gameWindow.asInstanceOf[GameBoardView].controller.selectBenchLocation(bench.size)
            } catch {
              case ex: Exception => PopupBuilder.openInvalidOperationMessage(board.gameWindow,ex.getMessage)
            }
          }
        }
      }
    }
  }
}

