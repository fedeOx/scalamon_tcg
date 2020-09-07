package view

import io.circe.generic.auto._
import io.circe.parser
import scalafx.Includes._
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.{Scene, SceneAntialiasing}
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{Button, TableCell, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, GridPane}

import scala.io.Source

case class Deck(id: String, name: String, types: Seq[String], cards: ObservableBuffer[Cards])

case class Cards(id: String, name: String, rarity: String, count: Int) {
  val idCard = new StringProperty(this, "id", id)
  val nameCard = new StringProperty(this, "lastName", name)
  val rarityCard = new StringProperty(this, "rarity", rarity)
  val countCard = new ObjectProperty(this, "count", count)
}

object DeckSelection extends Scene {

  val cssStyle = getClass.getResource("/style/deckSelection.css").toExternalForm
  val input = Source.fromFile(getClass.getResource("/jsons/BaseDeck.json").getFile).getLines().mkString.stripMargin
  var decks: Set[Deck] = Set.empty[Deck]


  parser.decode[Seq[Deck]](input) match {
    case Right(deck) => deck.map(d => decks += d)
    case Left(ex) => println(s"Something wrong ${ex.getMessage}")
  }

  var cardsTableItem = ObservableBuffer[Cards](decks.head.cards)
  val tableView = createTableView
  val deckPane = createDeckPanel
  var deckSelected = decks.head

  stylesheets += cssStyle
  root = new BorderPane {
    id = "deckSelection-pane"
    left = deckPane
    right = tableView
    bottom  = new Button {
      id = "startGame-btn"
      text = "Start Game"
      alignment = Pos.Center
      alignmentInParent = Pos.Center
      onAction = _=> {
        //go to game
        println("INVIO IL DECK "+ getChosenDeck)
        StartGameGui.getPrimaryStage().close()
        new GameBoardView
      }}
  }


  def createDeckButton(deckName: String): Button = {
    val deckButton: Button = new Button(deckName)
    deckButton.onAction = () => {
      cardsTableItem.clear()
      deckSelected = decks.filter(deck => deck.name == deckName).head
      cardsTableItem.addAll(deckSelected.cards)
      tableView.refresh()
    }
    deckButton.id = deckName
    deckButton.text = ""
    deckButton
  }

  def createTableView: TableView[Cards] = {
    val table = new TableView[Cards](cardsTableItem) {
      id = "tableView-Cards"
      alignmentInParent = Pos.Center
      columns ++= List(
        new TableColumn[Cards, String]() {
          text = "Card Name"
          cellValueFactory = {
            _.value.nameCard
          }
          prefWidth = 100
        },
        new TableColumn[Cards, String]() {
          text = "Rarity"
          cellValueFactory = {
            _.value.rarityCard
          }
          prefWidth = 100
        },
        new TableColumn[Cards, Int]() {
          text = "Count"
          cellValueFactory = {
            _.value.countCard
          }
          prefWidth = 100
        }
      )
    }
    table
  }

  def createDeckPanel: GridPane = {
    var columnIndexcnt = 0
    var rowIndexcnt = 0
    val pane = new GridPane {
      id = "gridPane"
      alignment = Pos.CenterLeft
      hgap = 10
      vgap = 10
      //create button for decks
      decks.foreach(deck => {
        add(createDeckButton(deck.name.replaceAll("\\s+", "")), columnIndexcnt, rowIndexcnt)
        columnIndexcnt += 1
        if (columnIndexcnt > 1) {
          columnIndexcnt = 0
          rowIndexcnt = 1
        }
      })
    }
    pane
  }

  def getChosenDeck : Deck ={
    deckSelected
  }
}


