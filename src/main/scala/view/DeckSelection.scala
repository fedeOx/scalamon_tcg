package view

import animatefx.animation.Flip
import common.Observer
import controller.Controller
import model.core.{DataLoader, GameManager}
import model.event.Events
import model.event.Events.Event.{BuildGameField, FlipCoin, ShowDeckCards}
import model.game.{DeckCard, DeckType, SetType}
import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{Button, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, GridPane}

case class CardView(id: String, name: String, rarity: String, count: Int) {
  val idCard = new StringProperty(this, "id", id)
  val nameCard = new StringProperty(this, "lastName", name)
  val rarityCard = new StringProperty(this, "rarity", rarity)
  val countCard = new ObjectProperty(this, "count", count)
}

object DeckSelection extends Scene with Observer{
  var cardsTableItem: ObservableBuffer[CardView] = ObservableBuffer[CardView]()
  val tableView: TableView[CardView] = createTableView
  val deckPane: GridPane = createDeckPanel
  val controller:Controller = Controller()
  val cssStyle: String = getClass.getResource("/style/deckSelection.css").toExternalForm
  stylesheets += cssStyle
  DataLoader.addObserver(this)
  controller.loadDeckCards(SetType.Base,DeckType.Base1)

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
        StartGameGui.getPrimaryStage.close()
        new GameBoardView

        var seqDeck:Seq[DeckCard] = Seq()
        cardsTableItem.foreach(p => seqDeck = seqDeck :+ DeckCard(p.id,p.name,p.rarity,p.count))
        controller.initGame(seqDeck,SetType.Base)
      }}
  }

  def createDeckButton(deckName: String): Button = {
    val deckButton: Button = new Button(deckName)
    deckButton.onAction = () => {
      controller.loadDeckCards(SetType.Base,DeckType.withNameWithDefault(deckName))
    }
    deckButton.id = deckName
    deckButton.text = ""
    deckButton
  }

  def createTableView: TableView[CardView] = {
    val table = new TableView[CardView](cardsTableItem) {
      id = "tableView-Cards"
      alignmentInParent = Pos.Center
      columns ++= List(
        new TableColumn[CardView, String] {
          text = "Card Name"
          cellValueFactory = {_.value.nameCard}
        },
        new TableColumn[CardView, String]() {
          text = "Rarity"
          cellValueFactory = {_.value.rarityCard}
        },
        new TableColumn[CardView, Int]() {
          text = "Count"
          cellValueFactory = {_.value.countCard}
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
      DeckType.values.foreach(deck => {
        add(createDeckButton(deck.name), columnIndexcnt, rowIndexcnt)
        columnIndexcnt += 1
        if (columnIndexcnt > 1) {
          columnIndexcnt = 0
          rowIndexcnt = 1
        }
      })
    }
    pane
  }

  override def update(event: Events.Event): Unit = event match {
    case event if event.isInstanceOf[ShowDeckCards] =>  {
      cardsTableItem.clear()
      event.asInstanceOf[ShowDeckCards].deckCards.foreach(deckCard => {
        cardsTableItem = cardsTableItem :+ CardView(deckCard.imageId, deckCard.name,
          deckCard.rarity, deckCard.count)
      })
      tableView.setItems(cardsTableItem)
      tableView.refresh()
    }
    case _ =>
  }
}

