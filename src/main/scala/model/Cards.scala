package model

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.Cards.EnergyCard.EnergyCardType.EnergyCardType
import model.EnergyType.EnergyType
import model.exception.MissingEnergyException

import scala.collection.mutable

object Cards {

  sealed trait Card {
    def imageId: String
  }

  sealed trait PokemonCard extends Card {
    def pokemonTypes: Seq[EnergyType]
    def name: String
    def initialHp: Int
    def actualHp: Int
    def actualHp_=(value: Int): Unit
    def weaknesses: Seq[Weakness]
    def resistances: Seq[Resistance]
    def retreatCost: Seq[EnergyType]
    def evolvesFrom: String
    def attacks: Seq[Attack]

    def addEnergy(energyCard: EnergyCard): Unit

    @throws(classOf[MissingEnergyException])
    def removeEnergy(energy: EnergyType): Unit

    def hasEnergies(energies: Seq[EnergyType]): Boolean

    def totalEnergiesStored: Int

    def addDamage(damage: Int, opponentTypes: Seq[EnergyType]): Unit

    def isKO: Boolean
  }

  object PokemonCard {
    def apply(imageId: String, pokemonTypes: Seq[EnergyType], name: String, initialHp: Int, weaknesses: Seq[Weakness],
              resistances: Seq[Resistance], retreatCost: Seq[EnergyType], evolvesFrom: String, attacks: Seq[Attack]): PokemonCard =
      PokemonCardImpl(imageId, pokemonTypes, name, initialHp, initialHp, weaknesses, resistances, retreatCost, evolvesFrom, attacks)

    implicit val decoder: Decoder[PokemonCard] = new Decoder[PokemonCard] {
      override def apply(c: HCursor): Result[PokemonCard] =
        for {
          _id <- c.downField("id").as[String]
          _pokemonTypes <- c.downField("types").as[Seq[EnergyType]]
          _name <- c.downField("name").as[String]
          _initHp <- c.downField("hp").as[String]
          _weaknesses <- c.getOrElse[Seq[Weakness]]("weaknesses")(Seq())
          _resistances <- c.getOrElse[Seq[Resistance]]("resistances")(Seq())
          _retreatCost <- c.getOrElse[Seq[EnergyType]]("retreatCost")(Seq())
          _evolvesFrom <- c.getOrElse[String]("evolvesFrom")("")
          _attacks <- c.downField("attacks").as[Seq[Attack]]
        } yield {
          val imageId = _id.replace(c.downField("setCode").as[String].getOrElse("") + "-", "")
          PokemonCard(imageId, _pokemonTypes, _name, _initHp.toInt, _weaknesses, _resistances, _retreatCost, _evolvesFrom, _attacks)
        }
    }

    case class PokemonCardImpl(override val imageId: String,
                               override val pokemonTypes: Seq[EnergyType],
                               override val name: String,
                               override val initialHp: Int,
                               override var actualHp: Int,
                               override val weaknesses: Seq[Weakness],
                               override val resistances: Seq[Resistance],
                               override val retreatCost: Seq[EnergyType],
                               override val evolvesFrom: String,
                               override val attacks: Seq[Attack],
                               private val energiesMap: mutable.Map[EnergyType, Int] = mutable.Map()) extends PokemonCard {

      override def addEnergy(energyCard: EnergyCard): Unit = energiesMap.get(energyCard.energyType) match {
        case Some(_) => energiesMap(energyCard.energyType) += energyCard.energiesProvided
        case None => energiesMap += (energyCard.energyType -> energyCard.energiesProvided)
      }

      @throws(classOf[MissingEnergyException])
      override def removeEnergy(energy: EnergyType): Unit = energiesMap.get(energy) match {
        case Some(e) if e>1 => energiesMap(energy) -= 1
        case Some(_) => energiesMap.remove(energy)
        case None => throw new MissingEnergyException()
      }

      override def hasEnergies(energies: Seq[EnergyType]): Boolean =
        !energies.foldLeft(mutable.Map[EnergyType, Int]().withDefaultValue(0)) {
          (map, energy) => map(energy) += 1; map
        }.exists(t => !energiesMap.contains(t._1) || (energiesMap.contains(t._1) && energiesMap(t._1) < t._2))

      def totalEnergiesStored: Int = energiesMap.values.sum

      override def addDamage(damage: Int, opponentTypes: Seq[EnergyType]): Unit = {
        import Weakness.Operation
        @scala.annotation.tailrec
        def weaknessLoss(loss: Int, weakSeq: Seq[Weakness]): Int = weakSeq match {
          case h :: t if opponentTypes.contains(h.energyType) && h.operation == Operation.multiply2 => weaknessLoss(damage, t)
          case _ :: t => weaknessLoss(loss, t)
          case _ => loss
        }
        @scala.annotation.tailrec
        def resistanceGain(reduction: Int, resSeq: Seq[Resistance]): Int = resSeq match {
          case h :: t if opponentTypes.contains(h.energyType) => resistanceGain(reduction + h.reduction, t)
          case _ :: t => resistanceGain(reduction, t)
          case _ => reduction
        }
        if (!this.isKO) {
          actualHp = actualHp - damage - weaknessLoss(0, weaknesses) + resistanceGain(0, resistances)
          if (actualHp < 0) actualHp = 0
          if (actualHp > initialHp) actualHp = initialHp
        }
      }

      override def isKO: Boolean = actualHp == 0
    }
  }

  sealed trait EnergyCard extends Card {
    def isBasic: Boolean
    def energyType: EnergyType
    def energiesProvided: Int
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

    def apply(imageId: String, energyType: EnergyType, energyCardType: EnergyCardType): EnergyCard =
      EnergyCardImpl(imageId, energyType, energyCardType)

    implicit val decoder: Decoder[EnergyCard] = new Decoder[EnergyCard] {
      override def apply(c: HCursor): Result[EnergyCard] =
        for {
          _id <- c.downField("id").as[String]
          _energyType <- c.downField("type").as[EnergyType]
          _energyCardType <- c.downField("subtype").as[EnergyCardType]
        } yield {
          val imageId = _id.replace(c.downField("setCode").as[String].getOrElse("") + "-", "")
          EnergyCard(imageId, _energyType, _energyCardType)
        }
    }

    case class EnergyCardImpl(override val imageId: String,
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
    }
  }

  // + a trait for TrainerCard
  // + a case class for TrainerCardImpl
}
