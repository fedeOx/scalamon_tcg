package view

import common.Observer
import controller.Controller
import model.event.Events
import model.event.Events.Event.ShowDeckCards
import model.game.SetType.SetType
import model.game.{DeckCard, SetType}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.TableColumn._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.text.Font
import view.game.GameBoardView

import scala.collection.immutable.ListMap


case class CardView(id: String, imageNumber: Int, name: String, rarity: String, var count: Int, set: SetType) {
  val idCard = new StringProperty(this, "id", id)
  val imageNumberCard = new IntegerProperty(this, "imageNumber", imageNumber)
  val nameCard = new StringProperty(this, "lastName", name)
  val rarityCard = new StringProperty(this, "rarity", rarity)
  var countCard = new ObjectProperty(this, "count", count)
  val setCard = new ObjectProperty(this, "set", set)
}

case class DeckSelection(controller: Controller) extends Scene with Observer {
  var cardsTableItem: ObservableBuffer[CardView] = ObservableBuffer[CardView]()
  val tableView: TableView[CardView] = viewUtils.createTableView(cardsTableItem)
  var deckMap: Map[String, Seq[DeckCard]] = Map()
  val scrollPane: ScrollPane = new ScrollPane()
  scrollPane.minWidth = 600
  scrollPane.minHeight = 900
  val cssStyle: String = getClass.getResource("/style/deckSelection.css").toExternalForm
  stylesheets += cssStyle
  controller.dataLoader.addObserver(this)

  controller.loadCustomDecks()
  SetType.values.foreach(set => {
    controller.loadDecks(set)
  })


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
          var setTypesSelected: Seq[SetType] = Seq()
          cardsTableItem.foreach(p => seqDeck = seqDeck :+ DeckCard(p.id, p.imageNumber, Some(p.set), p.name, p.rarity, p.count))
          seqDeck.foreach(card => {
            if (!setTypesSelected.contains(card.belongingSet.get)) {
              setTypesSelected = setTypesSelected :+ card.belongingSet.get
            }
          })
          controller.initGame(seqDeck, setTypesSelected)
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
      vgap = 20
      hgap = 20
      //padding = Insets(5, 20, 5, 20)
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

  override def update(event: Events.Event): Unit = event match {
    case event if event.isInstanceOf[ShowDeckCards] => {
      Platform.runLater(() => {
        deckMap = deckMap ++ event.asInstanceOf[ShowDeckCards].deckCards
        deckMap = ListMap(deckMap.toSeq.sortWith(_._1 < _._1):_*)
        scrollPane.content = createDeckPanel
      })
    }
    case _ =>
  }

  def createDeckItem(deckName: String): VBox = {
    val deckButton: Button = new Button(deckName)
    val selectedDeck = deckMap(deckName)
    deckButton.onAction = () => {
      cardsTableItem.clear()
      selectedDeck.foreach(card => {
        cardsTableItem = cardsTableItem :+ CardView(card.id, card.imageNumber, card.name, card.rarity, card.count, set = card.belongingSet.getOrElse(SetType.Base))
      })
      tableView.setItems(cardsTableItem)
      tableView.refresh()
    }
    deckButton.id = deckName
    deckButton.getStyleClass.add("deckSelection")
    deckButton.text = ""

    new VBox(deckButton, new Label(deckName) {
      font = Font.font(20);
      id = "deckNameLabel"
    }) {
      alignment = Pos.Center;
     // padding = Insets(5, 15, 5, 15)
    }
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
        new TableColumn[CardView, SetType]() {
          text = "Set"
          cellValueFactory = {
            _.value.setCard
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

