package model.card

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.card.EnergyCard.EnergyCardType.EnergyCardType
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType

sealed trait EnergyCard extends Card {
  def isBasic: Boolean
  def energyType: EnergyType
  def energiesProvided: Int

  def cloneEnergyCard: EnergyCard
}

object EnergyCard {
  object EnergyCardType extends Enumeration {
    type EnergyCardType = Value
    val basic: Value = Value("Basic")
    val special: Value = Value("Special")

    implicit val decoder: Decoder[EnergyCardType] = new Decoder[EnergyCardType] {
      override def apply(c: HCursor): Result[EnergyCardType] =
        for {
          t <- c.as[String]
        } yield {
          EnergyCardType.withName(t)
        }
    }
  }

  def apply(id: String, belongingSet: SetType, name: String, rarity: String,  setCode: String, energyType: EnergyType,
            energyCardType: EnergyCardType): EnergyCard = {
    val imageId = id.replace(setCode + "-", "").toInt
    EnergyCardImpl(id, imageId, belongingSet, name, rarity, setCode, energyType, energyCardType)
  }

  implicit val decoder: Decoder[EnergyCard] =
    Decoder.forProduct7("id", "set", "name", "rarity", "setCode",
      "type", "subtype")(EnergyCard.apply)

  case class EnergyCardImpl(override val id: String,
                            override val imageNumber: Int,
                            override val belongingSet : SetType,
                            override val name: String,
                            override val rarity: String,
                            override val belongingSetCode: String,
                            override val energyType: EnergyType,
                            private val energyCardType: EnergyCardType) extends EnergyCard {
    override def isBasic: Boolean = energyCardType match {
      case EnergyCardType.basic => true
      case _ => false
    }

    override def energiesProvided: Int = energyCardType match {
      case EnergyCardType.basic => 1
      case _ => 2
    }

    override def cloneEnergyCard: EnergyCard = copy(id, imageNumber, belongingSet, name, rarity, belongingSetCode, energyType, energyCardType)
  }
}
