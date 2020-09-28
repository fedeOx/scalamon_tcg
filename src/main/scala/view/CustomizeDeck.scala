package view


import common.Observer
import controller.Controller
import model.core.DataLoader
import model.event.Events
import model.event.Events.Event.ShowSetCards
import model.game.Cards.Card
import model.game.{CustomDeck, DeckCard}
import model.game.SetType.SetType
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.stage.Stage


case class CustomizeDeck(setType: SetType) extends Scene with Observer {

  DataLoader.addObserver(this)
  var deckCard: Seq[Card] = List()
  val loadingMessage: Stage = PopupBuilder.openLoadingScreen(window.getValue.asInstanceOf[scalafx.stage.Window])
  Platform.runLater(loadingMessage.show())

  //TODO PASSAMI IL CONTROLLER LORENZO SIMONCINI 2
  val controller: Controller = Controller()
  controller.loadSet(setType)
  val textFieldName: TextField = new TextField {
    maxWidth = 200
  }
  val buttonConfirm: Button = new Button {
    id = "Confirm-btn"
    text = "Salva"
    margin = Insets(0, 10, 0, 10)
    onAction = _ => {
      var seqDeck: Seq[DeckCard] = Seq()
      if (textFieldName.text.value != "") {
        var totalCard = 0
        cardsTableItem.foreach(p => {seqDeck = seqDeck :+ DeckCard(p.id, p.name, p.rarity, p.count) ; totalCard += p.count})
        if (totalCard >= 60) {
          controller.createCustomDeck(CustomDeck(textFieldName.text.value, setType, seqDeck))
          StartGameGui.getPrimaryStage.scene = new DeckSelection
        }
      }
    }
  }

  var cardsTableItem: ObservableBuffer[CardView] = ObservableBuffer[CardView]()
  val tableView: TableView[CardView] = viewUtils.createTableView(cardsTableItem)
  val scrollPane: ScrollPane = new ScrollPane()
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
    top = createTopInfo
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
      children = Seq(new Button("" + card.imageId) {
        id = "cardSelect";
        style = "-fx-background-image: url(/assets/base1/" + card.imageId + ".jpg)";
        text = ""
      },
        new HBox() {
          alignmentInParent = Pos.TopCenter
          alignment = Pos.TopCenter
          minHeight = 20
          minWidth = 20
          padding = Insets(5, 10, 5, 10)
          children = Seq(new Button("+") {
            onMouseClicked = _ => addOrRemove(true, card)
          }, new Button("-") {
            onMouseClicked = _ => addOrRemove(false, card)
          })
        })
    }
  }

  private def createTopInfo: HBox = {
    new HBox(new Button {
      id = "backButton";
      onMouseClicked = _ => {
        StartGameGui.getPrimaryStage.scene = new DeckSelection
      }
    }, new VBox(new HBox(new Label("Inserisci nome del deck ") {
      id = "deckNameLabel"; margin = Insets(0, 10, 0, 10)
    }, textFieldName), buttonConfirm) {
      padding = Insets(5, 0, 0, 30)
    })
  }

  private def addOrRemove(add: Boolean, card: Card): Unit = {
    if (cardsTableItem.exists(p => p.idCard.getValue == card.imageId)) {
      val pokemonSelected = cardsTableItem.find(p => p.idCard.getValue == card.imageId).get
      if (add)
        pokemonSelected.count += 1
      else {
        pokemonSelected.count -= 1
        cardsTableItem.removeIf(pkm => pkm.count == 0)
      }
      pokemonSelected.countCard = ObjectProperty(pokemonSelected.count)
    } else if (add) {
      cardsTableItem.add(CardView(card.imageId, card.name, card.rarity, 1))
    }
    tableView.setItems(cardsTableItem)
    tableView.refresh()
  }

  override def update(event: Events.Event): Unit = event match {
    case event if event.isInstanceOf[ShowSetCards] => {
      deckCard = event.asInstanceOf[ShowSetCards].setCards
      Platform.runLater(() => {
        scrollPane.content = createCardPanel
      })
    }
    case _ =>
  }
}
