package view


import common.Observer
import controller.Controller
import model.core.DataLoader
import model.event.Events
import model.event.Events.Event.ShowSetCards
import model.game.Cards.Card
import model.game.{DeckCard, SetType}
import scalafx.application.Platform
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._


object CustomDeck extends Scene with Observer {

  DataLoader.addObserver(this)
  var deckCard: Seq[Card] = List()

  //TODO PASSAMI IL CONTROLLER LORENZO SIMONCINI 2
  val controller: Controller = Controller()
  controller.loadSet(SetType.Base)
  val textFieldName: TextField = new TextField {
    maxWidth = 200
  }
  val buttonConfirm: Button = new Button {
    id = "Confirm-btn"
    text = "Salva"
    alignment = Pos.BaselineRight
    alignmentInParent = Pos.BaselineRight
    onAction = _ => {
      var seqDeck: Seq[DeckCard] = Seq()
      //TODO PIMPARE QUESTO
      cardsTableItem.foreach(p => seqDeck = seqDeck :+ DeckCard(p.id, p.name, p.rarity, p.count))
      controller.createCustomDeck(model.game.CustomDeck(textFieldName.text.value, SetType.Base,seqDeck))
    }
  }

  var cardsTableItem: ObservableBuffer[CardView] = ObservableBuffer[CardView]()
  val tableView: TableView[CardView] = viewUtils.createTableView(cardsTableItem)
  val scrollPane: ScrollPane = new ScrollPane()
  //val deckPane: GridPane = createCardPanel
  stylesheets = List("/style/deckSelection.css")

  // scrollPane.content = deckPane
  scrollPane.padding = Insets(5, 5, 5, 5)
  scrollPane.setBackground(Background.Empty)
  scrollPane.minWidth = 1050
  scrollPane.maxHeight = 900

  tableView.minHeight = 600
  tableView.minWidth = 250

  root = new BorderPane {
    id = "CardPane"
    padding = Insets(15, 15, 15, 15)
    bottom = new VBox(new HBox(new Label("Inserisci nome del deck  "), textFieldName) {
      alignment = Pos.TopCenter; padding = Insets(5, 5, 5, 5)
    }, buttonConfirm) {
      alignment = Pos.TopCenter
    }
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
    }
  }

  def createButtonCard(card: Card): VBox = {
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
          children = Seq(new Button("+") {
            onMouseClicked = _ => addAndRemove(true, card)
            padding = Insets(10, 10, 10, 10)
          }, new Button("-") {
            onMouseClicked = _ => addAndRemove(false, card)
            padding = Insets(10, 10, 10, 10)
          })
        })
    }
  }

  private def addAndRemove(add: Boolean, card: Card): Unit = {
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
