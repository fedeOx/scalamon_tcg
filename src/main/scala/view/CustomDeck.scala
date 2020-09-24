package view


import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._


object CustomDeck extends Scene {

  val textFieldName: TextField = new TextField {
    maxWidth = 200
  }
  val buttonConfirm: Button = new Button {
    id = "Confirm-btn"
    text = "Salva"
    alignment = Pos.BaselineRight
    alignmentInParent = Pos.BaselineRight
    onAction = _ => {
    }
  }
  var cardsTableItem: ObservableBuffer[CardView] = ObservableBuffer[CardView]()
  val tableView: TableView[CardView] = viewUtils.createTableView(cardsTableItem)
  val scrollPane: ScrollPane = new ScrollPane()
  val deckPane: GridPane = createCardPanel
  stylesheets = List("/style/deckSelection.css")

  scrollPane.content = deckPane
  scrollPane.padding = Insets(5, 5, 5, 5)
  scrollPane.setBackground(Background.Empty)
  scrollPane.minWidth = 1050
  scrollPane.maxHeight = 900

  tableView.minHeight = 600
  tableView.minWidth = 250

  root = new BorderPane {
    id = "CardPane"
    padding = Insets(15, 15, 15, 15)
    bottom = new VBox(new HBox(new Label("Inserisci nome del deck  "), textFieldName){alignment = Pos.TopCenter; padding = Insets(5,5,5,5)}, buttonConfirm){alignment = Pos.TopCenter}
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

      for (i <- 1 to 102) {
        add(createButtonCard(i.toString), columnIndexcnt, rowIndexcnt)
        columnIndexcnt += 1
        if (columnIndexcnt > 2) {
          columnIndexcnt = 0
          rowIndexcnt += 1
        }
      }
    }
  }

  def createButtonCard(cardId: String): VBox = {
    new VBox() {
      background = Background.Empty
      children = Seq(new Button("" + cardId) {
        id = "cardSelect";
        style = "-fx-background-image: url(/assets/base1/" + cardId + ".jpg)";
        text = ""
      },
        new HBox() {
          alignmentInParent = Pos.TopCenter
          alignment = Pos.TopCenter
          children = Seq(new Button("+") {
            onMouseClicked = _ => addAndRemove(true, cardId)
            padding = Insets(10,10,10,10)
          }, new Button("-") {
            onMouseClicked = _ => addAndRemove(false, cardId)
            padding = Insets(10,10,10,10)
          })
        })
    }
  }

  private def addAndRemove(add: Boolean, cardId: String): Unit = {
    if (cardsTableItem.exists(p => p.idCard.getValue == cardId)) {
      val pokemonSelected = cardsTableItem.find(p => p.idCard.getValue == cardId).get
      if (add)
        pokemonSelected.count += 1
      else {
        pokemonSelected.count -= 1
        cardsTableItem.removeIf(pkm => pkm.count == 0)
      }
      pokemonSelected.countCard = ObjectProperty(pokemonSelected.count)
    } else if (add)
      cardsTableItem.add(CardView(cardId, cardId, "SONDRIO", 1))

    tableView.setItems(cardsTableItem)
    tableView.refresh()
  }
}
