package view

import common.{Observer, TurnOwner}
import common.TurnOwner.TurnOwner
import controller.Controller
import javafx.scene.paint.ImagePattern
import model.core.{DataLoader, GameManager, TurnManager}
import model.event.Events
import model.event.Events.Event.{BuildGameField, FlipCoin, UpdatePlayerBoard}
import model.game.Cards.EnergyCard.EnergyCardType
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.Weakness.Operation
import model.game.Weakness.Operation.Operation
import model.game.{EnergyType, Resistance, SetType, StatusType, Weakness}
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Pos
import scalafx.scene.image.Image
import scalafx.scene.layout._
import scalafx.scene.paint.PhongMaterial
import scalafx.scene.shape.Box
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{Group, PerspectiveCamera, Scene, SceneAntialiasing}
import scalafx.stage.Window

/** *
 * Stage that contains the game scene
 */
class GameBoardView extends JFXApp.PrimaryStage with Observer {
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"
  private val parentWindow : Window = this
  val zoomZone = new ZoomZone
  private val opponentBoard = new PlayerBoard(false, zoomZone, parentWindow)
  private val humanBoard = new PlayerBoard(true, zoomZone, parentWindow)
  private var turnOwner : TurnOwner = TurnOwner.Player
  title = TITLE
  GameManager.addObserver(this)
  TurnManager.addObserver(this)
  x = 0
  y = 0
  println("sono gameboardview e mi registro")
  scene = new Scene(WIDTH, HEIGHT, true, SceneAntialiasing.Balanced) {
    println(parentWindow)
    stylesheets = List("/style/PlayerBoardStyle.css")
    camera = new PerspectiveCamera(true) {
      transforms += (
        new Rotate(50, Rotate.XAxis),
        new Translate(0, 5, -75))
    }
    val playMatMaterial = new PhongMaterial()
    playMatMaterial.diffuseMap = new Image("/assets/playmat.png")
    fill = new ImagePattern(new Image("/assets/woodTable.png"))

    content = List(new Box {
      width = 55
      height = 50
      depth = 0.4
      material = playMatMaterial
    }, new Group {
      translateX = -27.5
      translateY = -25
      minWidth(55)
      minHeight(50)
      alignmentInParent = Pos.Center
      children = Seq(opponentBoard, humanBoard)
    }, zoomZone)
  }

  resizable = true
  sizeToScene()
  show()

  override def update(event: Events.Event): Unit = event match {
    case event if  event.isInstanceOf[BuildGameField] => {
      humanBoard.board = event.asInstanceOf[BuildGameField].playerBoard
      opponentBoard.board = event.asInstanceOf[BuildGameField].opponentBoard
      Platform.runLater(humanBoard.updateHand())
      //TODO: togli tutto diomadonna
      val weakness: Weakness = new Weakness {
        override def energyType: EnergyType = EnergyType.Fighting
        override def operation: Operation = Operation.multiply2
      }
      val resistance: Resistance = new Resistance {
        override def energyType: EnergyType = EnergyType.Lightning
        override def reduction: Int = 30
      }
      val cardList: Seq[Card] = DataLoader.loadSet(SetType.Base)
        .filter(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].imageId.equals("6"))
      println(cardList)
      //var carta = PokemonCard("4", "base1",Seq(EnergyType.Colorless), "pokemonName", 100, Seq(weakness),
        //Seq(resistance), Seq(EnergyType.Colorless, EnergyType.Colorless), "", Nil)
      var carta = cardList.head.asInstanceOf[PokemonCard]
      carta.actualHp = 40
      carta.status = StatusType.Poisoned
      carta.addEnergy(EnergyCard("98","base1",EnergyType.Water, EnergyCardType.basic))
      carta.addEnergy(EnergyCard("98","base1",EnergyType.Water, EnergyCardType.basic))
      carta.addEnergy(EnergyCard("98","base1",EnergyType.Water, EnergyCardType.basic))
      /*carta.addEnergy(EnergyCard("99","base1",EnergyType.Grass, EnergyCardType.basic))
      carta.addEnergy(EnergyCard("98","base1",EnergyType.Fire, EnergyCardType.basic))
      carta.addEnergy(EnergyCard("98","base1",EnergyType.Fire, EnergyCardType.basic))
      carta.addEnergy(EnergyCard("99","base1",EnergyType.Grass, EnergyCardType.basic))*/
      //humanBoard.board.activePokemon = Some(carta)
      Platform.runLater(humanBoard.updateActive())
    }
    case event if event.isInstanceOf[FlipCoin] =>{
      turnOwner = event.asInstanceOf[FlipCoin].coinValue
      println("turno di : "+turnOwner)
    }
    case event : UpdatePlayerBoard => {
      humanBoard.updateActive()
      humanBoard.updateHand()
      humanBoard.updateBench()
    }
  }


}

//TODO: orribile
object utils {
  var controller = Controller()
}
