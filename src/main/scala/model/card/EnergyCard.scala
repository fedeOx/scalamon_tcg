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

  def apply(id: String, imageId: Int, belongingSet: SetType, name: String, rarity: String,  setCode: String, energyType: EnergyType,
            energyCardType: EnergyCardType): EnergyCard =
    EnergyCardImpl(id, imageId, belongingSet, name, rarity, setCode, energyType, energyCardType)

  implicit val decoder: Decoder[EnergyCard] = new Decoder[EnergyCard] {
    override def apply(c: HCursor): Result[EnergyCard] =
      for {
        _id <- c.downField("id").as[String]
        _name <- c.downField("name").as[String]
        _set <- c.downField("set").as[SetType]
        _rarity <- c.downField("rarity").as[String]
        _setCode <- c.downField("setCode").as[String]
        _energyType <- c.downField("type").as[EnergyType]
        _energyCardType <- c.downField("subtype").as[EnergyCardType]
      } yield {
        val imageId = _id.replace(c.downField("setCode").as[String].getOrElse("") + "-", "").toInt
        EnergyCard(_id, imageId, _set, _name, _rarity, _setCode, _energyType, _energyCardType)
      }
  }

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
