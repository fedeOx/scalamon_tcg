package view

import common.{Observable, Observer}
import controller.Controller
import model.core.DataLoader
import model.event.Events
import model.event.Events.Event.ShowDeckCards
import model.game.{DeckCard, SetType}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.TableColumn._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.text.Font

case class CardView(id: String, name: String, rarity: String, var count: Int) {
  val idCard = new StringProperty(this, "id", id)
  val nameCard = new StringProperty(this, "lastName", name)
  val rarityCard = new StringProperty(this, "rarity", rarity)
  var countCard = new ObjectProperty(this, "count", count)
}

case class DeckSelection(controller: Controller) extends Scene with Observer {
  var cardsTableItem: ObservableBuffer[CardView] = ObservableBuffer[CardView]()
  val tableView: TableView[CardView] = viewUtils.createTableView(cardsTableItem)
  var deckMap: Map[String, Seq[DeckCard]] = Map()
  val scrollPane: ScrollPane = new ScrollPane()
  val cssStyle: String = getClass.getResource("/style/deckSelection.css").toExternalForm
  stylesheets += cssStyle
  controller.dataLoader.addObserver(this)
  controller.loadDecks(SetType.Base)

  root = new BorderPane {
    id = "deckSelection-pane"
    left = scrollPane
    right = tableView
    bottom = new Button {
      id = "startGame-btn"
      text = "Start Game"
      alignment = Pos.Center
      alignmentInParent = Pos.Center
      onAction = _ => {
        //go to game
        if (cardsTableItem.nonEmpty) {
          GameLauncher.stage.close()
          new GameBoardView(controller)
          var seqDeck: Seq[DeckCard] = Seq()
          cardsTableItem.foreach(p => seqDeck = seqDeck :+ DeckCard(p.id, p.name, p.rarity, p.count))
          controller.initGame(seqDeck, SetType.Base)
        }
      }
    }
  }


  def createDeckPanel: GridPane = {
    var columnIndexcnt = 0
    var rowIndexcnt = 0

    new GridPane {
      background = Background.Empty
      id = "gridPane"
      alignment = Pos.CenterLeft
      hgap = 10
      vgap = 10
      padding = Insets(5, 20, 5, 20)
      val addDeckCustom: Button = new Button()
      addDeckCustom.id = "addDeck"
      addDeckCustom.text = ""
      addDeckCustom.onMouseClicked = _ => {
        GameLauncher.stage.scene = CustomizeDeck(SetType.Base, controller)
      }
      add(addDeckCustom, columnIndexcnt, rowIndexcnt)
      columnIndexcnt += 1

      deckMap.keys.foreach(k => {
        add(createDeckItem(k), columnIndexcnt, rowIndexcnt)
        columnIndexcnt += 1
        if (columnIndexcnt > 3) {
          columnIndexcnt = 0
          rowIndexcnt += 1
        }
      })
    }
  }

  def createDeckItem(deckName: String): VBox = {
    val deckButton: Button = new Button(deckName)
    val selectedDeck = deckMap(deckName)
    deckButton.onAction = () => {
      cardsTableItem.clear()
      selectedDeck.foreach(card => {
        cardsTableItem = cardsTableItem :+ CardView(card.imageId, card.name, card.rarity, card.count)
      })
      tableView.setItems(cardsTableItem)
      tableView.refresh()
    }
    deckButton.id = deckName
    deckButton.getStyleClass.add("deckSelection")
    deckButton.text = ""

    new VBox(deckButton, new Label(deckName) {
      font = Font.font(20); id = "deckNameLabel"
    }) {
      alignment = Pos.Center; padding = Insets(5, 15, 5, 15)
    }
  }

  override def update(event: Events.Event): Unit = event match {
    case event if event.isInstanceOf[ShowDeckCards] => {
      deckMap = event.asInstanceOf[ShowDeckCards].deckCards
      Platform.runLater(() => {
        scrollPane.content = createDeckPanel
      })
    }
    case _ =>
  }
}


object viewUtils {
  def createTableView(cardsTableItem: ObservableBuffer[CardView]): TableView[CardView] = {
    val table = new TableView[CardView](cardsTableItem) {
      id = "tableView-Cards"
      alignmentInParent = Pos.Center
      columns ++= List(
        new TableColumn[CardView, String] {
          text = "Card Name"
          cellValueFactory = {
            _.value.nameCard
          }
        },
        new TableColumn[CardView, String]() {
          text = "Rarity"
          cellValueFactory = {
            _.value.rarityCard
          }
        },
        new TableColumn[CardView, Int]() {
          text = "Count"
          cellValueFactory = {
            _.value.countCard
          }
        }
      )
    }
    table
  }

}

