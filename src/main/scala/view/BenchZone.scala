package view

import model.game.Cards.PokemonCard
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout.{BackgroundFill, HBox, Priority}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.Box
import view.CardCreator.createCard

import scala.collection.mutable

/***
 * The field zone that contains the benched pokemon
 * @param zone: the zone for the zoomed cards
 * @param isHumans: true if it's the human's board
 */
case class BenchZone(zone: ZoomZone, isHumans: Boolean, board: PlayerBoard) extends HBox {
  private val WIDTH = 35
  private val HEIGHT = 10
  private var bench : Seq[Box] = Seq()
  private var isEmpty : Boolean = _
  private val parentBoard = board
  var isOverChildren = false

  updateView()
  def updateView(cards: Seq[Option[PokemonCard]] = Seq()): Unit = {
    //TODO: check ultimo pkmn
    if (cards.isEmpty || (cards.count(c => c.isEmpty) == 5)) {
      println("sono qua")
      isEmpty = true
      val cardMaterial = new PhongMaterial()
      cardMaterial.diffuseColor = Color.Transparent
      bench = bench :+ new Box {
        material = cardMaterial
        depth = 0.5
      }
    } else {
      isEmpty = false
      bench = Seq[Box]()
      cards.filter(c => c.isDefined).zipWithIndex.foreach{case (card,cardIndex) => {
        bench = bench :+ createCard("/assets/"+card.get.belongingSetCode+"/"+card.get.imageId+".jpg", Some(zone), CardType.Bench,
          cardIndex = cardIndex, isHumans = Some(isHumans), zone = Some(this), board = Some(parentBoard.board))
      }}
    }
    children = bench
  }

  styleClass += "zz"
  spacing = 0.5
  minWidth = WIDTH
  minHeight = HEIGHT
  prefWidth = WIDTH
  prefHeight = HEIGHT
  alignment = Pos.Center
  translateX = 10
  translateY = 15
  onMouseClicked = _ => {
    if (isHumans && !isOverChildren && !bench.size.equals(5)) {
      if (isEmpty) {
        try {
          utils.controller.selectBenchLocation(0)
        } catch {
          case ex: Exception => PopupBuilder.openInvalidOperationMessage(board.parentWin,ex.getMessage)
        }
        println("panchina vuota")
      }
      else {
        try {
          utils.controller.selectBenchLocation(bench.size)
        } catch {
          case ex: Exception => PopupBuilder.openInvalidOperationMessage(board.parentWin,ex.getMessage)
        }

      }
    }
  }
}
