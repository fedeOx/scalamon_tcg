package model.game

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

import StatusType.StatusType
import model.exception.MissingEnergyException
import model.game.Cards.EnergyCard.EnergyCardType.EnergyCardType
import model.game.EnergyType.EnergyType
import scala.collection.mutable

object Cards {

  sealed trait Card {
    def imageId: String
    def belongingSetCode: String
  }

  sealed trait PokemonCard extends Card {
    def pokemonTypes: Seq[EnergyType]
    def name: String
    def initialHp: Int
    def actualHp: Int
    def actualHp_=(value: Int): Unit
    def immune: Boolean
    def immune_=(value: Boolean): Unit
    def status: StatusType
    def status_=(value: StatusType): Unit
    def weaknesses: Seq[Weakness]
    def resistances: Seq[Resistance]
    def retreatCost: Seq[EnergyType]
    def evolutionName: String
    def attacks: Seq[Attack]
    def energiesMap: Map[EnergyType, Int]
    def energiesMap_=(energies: Map[EnergyType, Int]): Unit

    def addEnergy(energyCard: EnergyCard): Unit

    @throws(classOf[MissingEnergyException])
    def removeEnergy(energy: EnergyType): Unit
    def removeFirstNEnergy(nEnergies : Int) : Unit

    def hasEnergies(energies: Seq[EnergyType]): Boolean

    def totalEnergiesStored: Int

    def addDamage(damage: Int, opponentTypes: Seq[EnergyType]): Unit

    def isKO: Boolean

    def isBase: Boolean
  }

  object PokemonCard {
    def apply(imageId: String, setCode: String, pokemonTypes: Seq[EnergyType], name: String, initialHp: Int, weaknesses: Seq[Weakness],
              resistances: Seq[Resistance], retreatCost: Seq[EnergyType], evolvesFrom: String, attacks: Seq[Attack]): PokemonCard =
      PokemonCardImpl(imageId, setCode, pokemonTypes, name, initialHp, initialHp, weaknesses, resistances, retreatCost, evolvesFrom, attacks, immune = false, StatusType.NoStatus)

    implicit val decoder: Decoder[PokemonCard] = new Decoder[PokemonCard] {
      override def apply(c: HCursor): Result[PokemonCard] =
        for {
          _id <- c.downField("id").as[String]
          _setCode <- c.downField("setCode").as[String]
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
          PokemonCard(imageId, _setCode, _pokemonTypes, _name, _initHp.toInt, _weaknesses, _resistances, _retreatCost, _evolvesFrom, _attacks)
        }
    }

    case class PokemonCardImpl(override val imageId: String,
                               override val belongingSetCode: String,
                               override val pokemonTypes: Seq[EnergyType],
                               override val name: String,
                               override val initialHp: Int,
                               override var actualHp: Int,
                               override val weaknesses: Seq[Weakness],
                               override val resistances: Seq[Resistance],
                               override val retreatCost: Seq[EnergyType],
                               override val evolutionName: String,
                               override val attacks: Seq[Attack],
                               override var immune: Boolean,
                               override var status : StatusType,
                               override var energiesMap: Map[EnergyType, Int] = Map()) extends PokemonCard {

      object MyMapHelpers { // pimp my library to improve cast between mutable and immutable maps
        implicit class MyImmutableMap[A, B](map: Map[A, B]) {
          def toMutableMap: mutable.Map[A, B] = mutable.Map(map.toSeq: _*)
        }
        implicit class MyMutableMap[A, B](map: mutable.Map[A, B]) {
          def toImmutableMap: Map[A, B] = Map(map.toSeq: _*)
        }
      }
      import MyMapHelpers._

      override def addEnergy(energyCard: EnergyCard): Unit = {
        val mutableEnergiesMap = energiesMap.toMutableMap
        if (energiesMap.get(energyCard.energyType).nonEmpty) {
          mutableEnergiesMap(energyCard.energyType) += energyCard.energiesProvided
        } else {
          mutableEnergiesMap += (energyCard.energyType -> energyCard.energiesProvided)
        }
        energiesMap = mutableEnergiesMap.toImmutableMap
      }

      @throws(classOf[MissingEnergyException])
      override def removeEnergy(energy: EnergyType): Unit = {
        def _removeEnergy(mutableEnergiesMap: mutable.Map[EnergyType, Int]): mutable.Map[EnergyType, Int] =
          mutableEnergiesMap.get(energy) match {
            case Some(e) if e>1 => mutableEnergiesMap(energy) -= 1; mutableEnergiesMap
            case Some(_) => mutableEnergiesMap.remove(energy); mutableEnergiesMap
            case None => throw new MissingEnergyException("There is not an energy of the specified type that can be removed")
        }
        energiesMap = _removeEnergy(energiesMap.toMutableMap).toImmutableMap
      }

      override def hasEnergies(energies: Seq[EnergyType]): Boolean =
        !energies.foldLeft(mutable.Map[EnergyType, Int]().withDefaultValue(0)) {
          (map, energy) => map(energy) += 1; map
        }.exists(t => !energiesMap.contains(t._1) || (energiesMap.contains(t._1) && energiesMap(t._1) < t._2))

      def totalEnergiesStored: Int = energiesMap.values.sum

      override def addDamage(damage: Int, opponentTypes: Seq[EnergyType]): Unit = {
        import Weakness.Operation
        @scala.annotation.tailrec
        def damageWithWeaknesses(damage: Int, weakSeq: Seq[Weakness]): Int = weakSeq match {
          case h :: t if opponentTypes.contains(h.energyType) && h.operation == Operation.multiply2 => damageWithWeaknesses(damage*2, t)
          case _ :: t => damageWithWeaknesses(damage, t)
          case _ => damage
        }
        @scala.annotation.tailrec
        def damageWithResistances(damage: Int, resSeq: Seq[Resistance]): Int = resSeq match {
          case h :: t if opponentTypes.contains(h.energyType) => damageWithResistances(damage - h.reduction, t)
          case _ :: t => damageWithResistances(damage, t)
          case _ => damage
        }
        if (!this.isKO) {
          var realDamage = damageWithResistances(damageWithWeaknesses(damage, weaknesses), resistances)
          if (realDamage < 0) realDamage = 0
          actualHp = actualHp - realDamage
          if (actualHp < 0) actualHp = 0
        }
      }

      override def isKO: Boolean = actualHp == 0

      override def removeFirstNEnergy(nEnergies: Int): Unit = {
        @scala.annotation.tailrec
        def remove(energyLeft : Int) : Unit = energyLeft match {
          case 0 =>
          case _ => removeEnergy(energiesMap.head._1)  ;  remove(energyLeft-1)
        }
        remove(nEnergies)
      }

      override def isBase: Boolean = evolutionName == ""
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

    def apply(imageId: String, setCode: String, energyType: EnergyType, energyCardType: EnergyCardType): EnergyCard =
      EnergyCardImpl(imageId, setCode, energyType, energyCardType)

    implicit val decoder: Decoder[EnergyCard] = new Decoder[EnergyCard] {
      override def apply(c: HCursor): Result[EnergyCard] =
        for {
          _id <- c.downField("id").as[String]
          _setCode <- c.downField("setCode").as[String]
          _energyType <- c.downField("type").as[EnergyType]
          _energyCardType <- c.downField("subtype").as[EnergyCardType]
        } yield {
          val imageId = _id.replace(c.downField("setCode").as[String].getOrElse("") + "-", "")
          EnergyCard(imageId, _setCode, _energyType, _energyCardType)
        }
    }

    case class EnergyCardImpl(override val imageId: String,
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
    }
  }

  // + a trait for TrainerCard
  // + a case class for TrainerCardImpl
}
