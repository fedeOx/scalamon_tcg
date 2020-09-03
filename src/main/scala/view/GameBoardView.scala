package view

import com.sun.javafx.sg.prism.NGNode
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Point3D
import scalafx.scene.image.Image
import scalafx.scene.{Camera, Group, PerspectiveCamera, Scene, SceneAntialiasing}
import scalafx.scene.layout.{Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize, GridPane, Pane}
import scalafx.scene.paint.Color._
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene._
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.{Box, DrawMode}
import scalafx.scene.transform.Transform._
import scalafx.scene.transform.{Rotate, Translate}

import scalafx.scene.transform.Transform._

import scalafx.scene.transform.{Rotate, Translate}

class GameBoardView extends JFXApp.PrimaryStage {
  private val WIDTH = 1600
  private val HEIGHT = 1000
  private val TITLE = "Scalamon"
  /*private val utilityPanel: UtilityPanel = new UtilityPanel
  private val boardAndPlayerPanel: BoardAndPlayerPanel = new BoardAndPlayerPanel(cards)
  private val legendPanel = new LegendPanel(users)*/
  title = TITLE
  scene = new Scene(WIDTH, HEIGHT, true, SceneAntialiasing.Balanced) {
    stylesheets = List("/style/PlayerBoardStyle.css")
    /*camera = new PerspectiveCamera(true) {
      transforms +=(
        new Rotate(-20, Rotate.YAxis),
        new Rotate(-20, Rotate.XAxis))
    }*/
    root = new Pane() {
      styleClass += "body"
      jfxBackgroundImage2sfx(new BackgroundImage(new Image("/assets/playmat.png"), BackgroundRepeat.NoRepeat,
        BackgroundRepeat.NoRepeat, BackgroundPosition.Default, BackgroundSize.Default))

      /*val box = new Box {
        width = 1600
        height = 1000
        depth = 5

        children = new GridPane() {
          /*add(new Rectangle{

            width = 1600
            height = 450
            fill <== when(hover) choose Green otherwise Transparent
          },0,0)*/
          add(new HumanPlayerBoard(false) {
            minWidth = 1600
            minHeight = 450
          }, 0, 0)
          add(new HumanPlayerBoard(true), 0, 1)
        }
      }*/
      children = new GridPane() {
        /*add(new Rectangle{

          width = 1600
          height = 450
          fill <== when(hover) choose Green otherwise Transparent
        },0,0)*/
        add(new HumanPlayerBoard(false) {
          minWidth = 1600
          minHeight = 450
        }, 0, 0)
        add(new HumanPlayerBoard(true), 0, 1)
      }
    }
  }

  resizable = false
  sizeToScene()
  show()
}
