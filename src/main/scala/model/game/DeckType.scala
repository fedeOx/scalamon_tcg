package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.game.SetType.SetType

object DeckType extends Enumeration {
  protected case class Val(name: String, setType: SetType) extends super.Val
  type DeckType = Val
  val Base1: DeckType = Val("Overgrowth", SetType.Base)
  val Base2: DeckType = Val("Zap", SetType.Base)
  val Base3: DeckType = Val("Brushfire", SetType.Base)
  val Base4: DeckType = Val("Blackout", SetType.Base)

  implicit def valueToDeckTypeVal(x: Value): Val = x.asInstanceOf[Val]

  implicit val decoder: Decoder[DeckType] = new Decoder[DeckType] {
    override def apply(c: HCursor): Result[DeckType] =
      for {
        t <- c.as[String]
      } yield {
        DeckType.values.find(_.name == t).get
      }
  }

  def withNameWithDefault(searchedName: String): Value =
    values.find(name => name.name == searchedName).getOrElse(DeckType.Base1)
}

