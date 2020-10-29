package view

import common.Events.{CustomDeckSavedEvent, ShowSetCardsEvent}
import common.{Events, Observer}
import controller.Controller
import model.card.Card
import model.game.{CustomDeck, DeckCard, SetType}
import model.game.SetType.SetType
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.stage.{Stage, Window}


case class CustomizeDeckView(setType: SetType, controller: Controller) extends Scene with Observer {

  controller.dataLoader.addObserver(this)
  var deckCard: Seq[Card] = List()
  val parentWindow: Window = window.getValue.asInstanceOf[scalafx.stage.Window]
  var loadingMessage: Stage = PopupBuilder.openLoadingScreen(parentWindow, showWheel = false)
  loadingMessage.show()
  controller.loadSet(setType)
  val textFieldName: TextField = new TextField {maxWidth = 200}
  val buttonConfirm: Button = new Button {
    id = "Confirm-btn"
    text = "Save"
    margin = Insets(0, 10, 0, 10)
    onAction = _ => {
      var seqDeck: Seq[DeckCard] = Seq()
      if (textFieldName.text.value != "") {
        var totalCard = 0
        cardsTableItem.foreach(p => {
          seqDeck = seqDeck :+ DeckCard(p.id, Some(p.set), p.name, p.rarity, p.count); totalCard += p.count
        })
        if (totalCard >= 60) {
          controller.createCustomDeck(CustomDeck(textFieldName.text.value, seqDeck))
        } else {
          PopupBuilder.openInvalidOperationMessage(parentWindow, "A deck must have at least 60 cards!")
        }
      } else {
        PopupBuilder.openInvalidOperationMessage(parentWindow, "Insert the deck name!")
      }
    }
  }
  var cardsTableItem: ObservableBuffer[CardView] = ObservableBuffer[CardView]()
  val tableView: TableView[CardView] = viewUtils.createTableView(cardsTableItem)
  val scrollPane: ScrollPane = new ScrollPane()
  val boxDeckSelect: ComboBox[String] = new ComboBox()

  boxDeckSelect.getItems.addAll("base", "fossil")
  boxDeckSelect.setValue("base")
  boxDeckSelect.onAction = _ => {
    loadingMessage = PopupBuilder.openLoadingScreen(parentWindow, showWheel = false)
    loadingMessage.show()
    controller.loadSet(SetType.withName(boxDeckSelect.getValue))
  }
  stylesheets = List("/style/deckSelection.css")

  scrollPane.padding = Insets(5, 5, 5, 5)
  scrollPane.setBackground(Background.Empty)
  scrollPane.minWidth = 1050
  scrollPane.maxHeight = 900
  tableView.minHeight = 600
  tableView.minWidth = 250

  root = new BorderPane {
    id = "CardPane"
    padding = Insets(15, 15, 15, 15)
    top = createTopInfoBox
    right = tableView
    left = scrollPane
  }

  def createCardPanel: GridPane = {
    var columnIndexcnt = 0
    var rowIndexcnt = 0
    new GridPane {
      background = Background.Empty
      alignment = Pos.CenterLeft
      hgap = 30
      vgap = 20
      deckCard.foreach(card => {
        add(createButtonCard(card), columnIndexcnt, rowIndexcnt)
        columnIndexcnt += 1
        if (columnIndexcnt > 2) {
          columnIndexcnt = 0
          rowIndexcnt += 1
        }
      })
      Platform.runLater(PopupBuilder.closeLoadingScreen(loadingMessage))
    }
  }

  private def createButtonCard(card: Card): VBox = {
    new VBox() {
      background = Background.Empty
      children = Seq(new Button("" + card.imageNumber) {
        id = "cardSelect";
        style = "-fx-background-image: url(/assets/" + card.belongingSetCode + "/" + card.imageNumber + ".png)";
        text = ""
      },
        new HBox() {
          alignmentInParent = Pos.TopCenter
          alignment = Pos.TopCenter
          minHeight = 20
          minWidth = 20
          padding = Insets(5, 10, 5, 10)
          children = Seq(new Button("+") {
            onMouseClicked = _ => addOrRemove(add = true, card)
          }, new Button("-") {
            onMouseClicked = _ => addOrRemove(add = false, card)
          })
        })
    }
  }

  private def createTopInfoBox: HBox = {
    new HBox(new Button {
      id = "backButton";
      onMouseClicked = _ => {
        GameLauncher.stage = new DeckSelection(controller)
      }
    }, new VBox(new HBox(new Label("Deck name: ") {
      id = "deckNameLabel";
    }, textFieldName) {
      spacing = 10
      margin = Insets(0, 10, 0, 10)
    }, new HBox(boxDeckSelect, buttonConfirm) {
      spacing = 10
      margin = Insets(10, 10, 0, 10)
    }) {
      padding = Insets(5, 0, 0, 30)
    })
  }

  private def addOrRemove(add: Boolean, card: Card): Unit = {
    if (cardsTableItem.exists(p => p.id == card.id && p.set == card.belongingSet)) {
      val pokemonSelected = cardsTableItem.find(p => p.id == card.id && p.set == card.belongingSet).get
      if (add)
        pokemonSelected.count += 1
      else {
        pokemonSelected.count -= 1
        cardsTableItem.removeIf(pkm => pkm.count == 0)
      }
      pokemonSelected.countCard = ObjectProperty(pokemonSelected.count)
    } else if (add) {
      cardsTableItem.add(CardView(card.id, card.name, card.rarity, 1, card.belongingSet))
    }
    tableView.setItems(cardsTableItem)
    tableView.refresh()
  }

  override def update(event: Events.Event): Unit = event match {
    case event if event.isInstanceOf[ShowSetCardsEvent] =>
      deckCard = event.asInstanceOf[ShowSetCardsEvent].setCards
      Platform.runLater(() => {
        scrollPane.content = createCardPanel
      })
    case event if event.isInstanceOf[CustomDeckSavedEvent] =>
      Platform.runLater(() => {
        if (event.asInstanceOf[CustomDeckSavedEvent].success) {
          GameLauncher.stage = new DeckSelection(controller)
        } else {
          PopupBuilder.openInvalidOperationMessage(parentWindow, "Deck name already present")
        }
      })
    case _ =>
  }
}
