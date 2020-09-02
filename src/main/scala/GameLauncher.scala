import scalafx.application.JFXApp
import view.StartGameGui

import scala.io.Source

object GameLauncher extends JFXApp {
  //val source = scala.io.Source.fromFile("/style/deckSelection.css")
  Source.fromFile(getClass.getResource("/jsons/BaseDeck.json").getFile).getLines().foreach(println)

}