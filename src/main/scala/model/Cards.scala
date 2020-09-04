package model

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import javafx.stage.Stage
import model.EnergyType.EnergyType

object Cards {

  sealed trait Card {
    def imageId: String
  }

  case class PokemonCard(override val imageId: String,
                    val cardTypes: Seq[EnergyType],
                    val name: String,
                    val initialHp: Int,
                    val actualHp: Int,
                    val weaknesses: Seq[Weakness],
                    val resistance: Seq[Resistance],
                    val retreatCost: Int,
                    val evolvesFrom: String,
                    val attacks: Seq[Attack],
                    val energies: Map[EnergyType, Int] = Map()) extends Card

  object PokemonCard {
    implicit val decoder: Decoder[PokemonCard] = new Decoder[PokemonCard] {
      override def apply(c: HCursor): Result[PokemonCard] =
        for {
          id <- c.downField("id").as[String]
          cardTypes <- c.downField("types").as[Seq[EnergyType]]
          name <- c.downField("name").as[String]
          initHp <- c.downField("hp").as[String]
          weaknesses <- c.getOrElse[Seq[Weakness]]("weaknesses")(Seq())
          resistances <- c.getOrElse[Seq[Resistance]]("resistances")(Seq())
          retreatCost <- c.getOrElse[Int]("convertedRetreatCost")(0)
          evolvesFrom <- c.getOrElse[String]("evolvesFrom")("")
          attacks <- c.downField("attacks").as[Seq[Attack]]
        } yield {
          val imageId = id.replace(c.downField("setCode").as[String].getOrElse("") + "-", "")
          PokemonCard(imageId, cardTypes, name, initHp.toInt, initHp.toInt, weaknesses, resistances, retreatCost, evolvesFrom, attacks)
        }
    }
  }

  sealed trait EnergyCard extends Card {

  }

  // + a trait for TrainerCard

  case class EnergyCardImpl(override val imageId: String) extends EnergyCard

  // + a case class for TrainerCardImpl

}
