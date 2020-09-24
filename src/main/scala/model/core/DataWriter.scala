package model.core

import java.io.{File, PrintWriter}
import java.nio.file.Files

import model.game.{CustomDeck, DeckCard}
import model.game.SetType.SetType
import io.circe.syntax._
import org.apache.commons.io.FileUtils

import scala.io.Source

object DataWriter {

  def saveCustomDeck(deck: CustomDeck): Unit = {
    // writing logic
    //println(getClass.getResource("/jsons").getPath + "/d_custom.json")
    System.getenv("APPDATA")
    val pw = new PrintWriter("/resources/jsons/d_custom.json")
    pw.write(deck.asJson.toString())
    pw.close()
    //FileUtils.writeStringToFile(new File(getClass.getResource("/jsons") + "/d_custom.json"),
      //deck.asJson.toString(), "UTF-8", true)
  }

}
