package model.pokemonEffect

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.Cards.PokemonCard
import model.pokemonEffect.EffectType.effectType

trait AttackEffect {
  def useEffect(): Unit
  var args: Map[String,Any]
  var totalDmgToEnemyPkm :Int

  def getIntArgFromMap(keyToFind : String):Int =  args.get(keyToFind).head.asInstanceOf[Int]
  def getStringArgFromMap(keyToFind : String):String =  args.get(keyToFind).head.asInstanceOf[String]

}

trait Effect {
  def name:effectType
  def params:Seq[String]
}
object Effect {
  implicit val decoder: Decoder[Effect] = new Decoder[Effect]{
    override def apply(c: HCursor): Result[Effect] =
    for {
      _name <- c.as[Option[effectType]]
      _params <- c.downField("params").as[Seq[String]]
    } yield {
      new Effect {
        override def name: effectType = _name.get
        override def params: Seq[String] = _params
      }
    }
  }
}

