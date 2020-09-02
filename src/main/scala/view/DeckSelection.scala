package view

import Deck.getClass
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.{BorderPane, GridPane}
import io.circe.generic.auto._
import io.circe.parser


import scala.io.Source

case class Deck(id: String, name: String, types: Seq[String], cards: Seq[Cards])

case class Cards(id: String, name: String, rarity: String, count: Int)


object DeckSelection extends PrimaryStage {
  val cssStyle = getClass.getResource("/style/deckSelection.css").toExternalForm
  val input = Source.fromFile(getClass.getResource("/jsons/BaseDeck.json").getFile).getLines().mkString.stripMargin
  var decks: Set[Deck] = Set.empty[Deck]
  title = "ScalaMon TCG"
  width = 900
  height = 800

  parser.decode[Seq[Deck]](input) match {
    case Right(deck) => deck.map(d => decks += d)
    case Left(ex) => println(s"Something wrong ${ex.getMessage}")
  }

  println(decks)
  scene = new Scene {
    stylesheets += cssStyle

    root = new BorderPane {
      id = "deckSelection-pane"

      left = new GridPane {
        id = "gridPane"
        alignment = Pos.Center
        hgap = 10
        vgap = 10
        add(createDeckButton("Zap"), 0, 0)
        add(createDeckButton("OverGrowth"), 0, 1)
        add(createDeckButton("BlackOut"), 1, 0)
        add(createDeckButton("BrushFire"), 1, 1)
      }
    }
  }

  def createDeckButton(deckName: String): Button = {
    val deckButton: Button = new Button(deckName)
    deckButton.onAction = () => {
      println("Deck selezionato " + deckName)
    }
    deckButton.id = deckName
    deckButton.text = ""
    deckButton
  }
}

/*
      implicit val decoder: Decoder[Deck] = new Decoder[Deck] {
        override def apply(hCursor: HCursor): Result[Deck] =
          for {
            id <- hCursor.get[String]("id")
            name <- hCursor.get[String]("name")
            types <- hCursor.downField("types").as[List[String]]
            cardsJson <- hCursor.downField("cards").as[List[Cards]]

            /*cards <- Traverse[List].traverse(cardsJson)(cardsJson => {
               cardsJson.hcursor.get[String]("id")
               cardsJson.hcursor.get[String]("name")
               cardsJson.hcursor.get[String]("rarity")
               cardsJson.hcursor.downField("count").as[Int]
            } ).as(List[Cards])*/
          } yield {
            Deck(id,name, types,cardsJson)
          }
      }

      parser.decode[List[Deck]](input) match {
        case Right(vouchers) => vouchers.map(println)
        case Left(ex) => println(s"Something wrong ${ex.getMessage}")
      }
*/

