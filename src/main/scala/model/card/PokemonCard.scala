package model.card

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import model.exception.InvalidOperationException
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType
import model.game.StatusType.StatusType
import model.game.{Attack, EnergyType, Resistance, StatusType, Weakness}

import scala.collection.mutable

trait PokemonCard extends Card {
  def pokemonTypes: Seq[EnergyType]
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
  def damageModifier: Int
  def damageModifier_=(value: Int)

  def addEnergy(energyCard: EnergyCard): Unit

  @throws(classOf[InvalidOperationException])
  def removeEnergy(energy: EnergyType): Unit

  def removeFirstNEnergies(nEnergies: Int): Unit

  def hasEnergies(energies: Seq[EnergyType]): Boolean

  def totalEnergiesStored: Int

  def addDamage(damage: Int, opponentTypes: Seq[EnergyType]): Unit

  def isKO: Boolean

  def isBase: Boolean

  def clonePokemonCard: PokemonCard
}

object PokemonCard {
  def apply(id: String, imageId: Int, belongingSet: SetType, setCode: String, rarity: String, pokemonTypes: Seq[EnergyType],
            name: String, initialHp: Int, weaknesses: Seq[Weakness], resistances: Seq[Resistance], retreatCost: Seq[EnergyType],
            evolvesFrom: String, attacks: Seq[Attack]): PokemonCard =
    PokemonCardImpl(id, imageId, belongingSet, setCode, rarity, pokemonTypes, name, initialHp, initialHp, weaknesses,
      resistances, retreatCost, evolvesFrom, attacks)

  implicit val decoder: Decoder[PokemonCard] = new Decoder[PokemonCard] {
    override def apply(c: HCursor): Result[PokemonCard] =
      for {
        _id <- c.downField("id").as[String]
        _set <- c.downField("set").as[SetType]
        _setCode <- c.downField("setCode").as[String]
        _rarity <- c.downField("rarity").as[String]
        _pokemonTypes <- c.downField("types").as[Seq[EnergyType]]
        _name <- c.downField("name").as[String]
        _initHp <- c.downField("hp").as[String]
        _weaknesses <- c.getOrElse[Seq[Weakness]]("weaknesses")(Seq())
        _resistances <- c.getOrElse[Seq[Resistance]]("resistances")(Seq())
        _retreatCost <- c.getOrElse[Seq[EnergyType]]("retreatCost")(Seq())
        _evolvesFrom <- c.getOrElse[String]("evolvesFrom")("")
        _attacks <- c.downField("attacks").as[Seq[Attack]]
      } yield {
        val imageNumber = _id.replace(c.downField("setCode").as[String].getOrElse("") + "-", "").toInt
        PokemonCard(_id, imageNumber, _set, _setCode, _rarity, _pokemonTypes, _name, _initHp.toInt, _weaknesses, _resistances, _retreatCost, _evolvesFrom, _attacks)
      }
  }

  case class PokemonCardImpl(override val id: String,
                             override val imageNumber: Int,
                             override val belongingSet: SetType,
                             override val belongingSetCode: String,
                             override val rarity: String,
                             override val pokemonTypes: Seq[EnergyType],
                             override val name: String,
                             override val initialHp: Int,
                             override var actualHp: Int,
                             override val weaknesses: Seq[Weakness],
                             override val resistances: Seq[Resistance],
                             override val retreatCost: Seq[EnergyType],
                             override val evolutionName: String,
                             override val attacks: Seq[Attack],
                             override var damageModifier: Int = 0,
                             override var immune: Boolean = false,
                             override var status: StatusType = StatusType.NoStatus,
                             override var energiesMap: Map[EnergyType, Int] = Map()) extends PokemonCard {

    import common.MyMapHelpers._

    override def addEnergy(energyCard: EnergyCard): Unit = {
      val mutableEnergiesMap = energiesMap.toMutableMap
      if (energiesMap.get(energyCard.energyType).nonEmpty) {
        mutableEnergiesMap(energyCard.energyType) += energyCard.energiesProvided
      } else {
        mutableEnergiesMap += (energyCard.energyType -> energyCard.energiesProvided)
      }
      energiesMap = mutableEnergiesMap.toImmutableMap
    }
    
    override def removeEnergy(energy: EnergyType): Unit = {
      def _removeEnergy(mutableEnergiesMap: mutable.Map[EnergyType, Int]): mutable.Map[EnergyType, Int] =
        mutableEnergiesMap.get(energy) match {
          case Some(e) if e>1 => mutableEnergiesMap(energy) -= 1; mutableEnergiesMap
          case Some(_) => mutableEnergiesMap.remove(energy); mutableEnergiesMap
          case None => throw new InvalidOperationException("There is not an energy of the specified type that can be removed")
        }
      energiesMap = _removeEnergy(energiesMap.toMutableMap).toImmutableMap
    }

    override def hasEnergies(energies: Seq[EnergyType]): Boolean = {
      var areEnergiesEnough = true
      val availableEnergiesMap: mutable.Map[EnergyType, Int] = mutable.Map[EnergyType, Int]() ++= energiesMap
      val requiredEnergiesMap: mutable.Map[EnergyType, Int] = energies.foldLeft(mutable.Map[EnergyType, Int]().withDefaultValue(0)) {
        (map, energy) => map(energy) += 1; map
      }

      for ((k, v) <- requiredEnergiesMap) {
        if (availableEnergiesMap.get(k).nonEmpty && v > 0) {
          val tmp = requiredEnergiesMap(k)
          requiredEnergiesMap(k) = v - availableEnergiesMap.getOrElse(k, 0)
          availableEnergiesMap(k) = availableEnergiesMap(k) - tmp
        }
      }
      areEnergiesEnough = requiredEnergiesMap.filter(t => t._1 != EnergyType.Colorless).values.sum <= 0 // Normal energies are enough
      if (areEnergiesEnough && requiredEnergiesMap.exists(t => t._1 == EnergyType.Colorless && t._2 > 0) ) {
        // Colorless check
        areEnergiesEnough = availableEnergiesMap.values.filter(v => v > 0).sum >= requiredEnergiesMap(EnergyType.Colorless)
      }
      areEnergiesEnough
    }

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
      if (!this.isKO && !immune) {
        var realDamage = damageWithResistances(damageWithWeaknesses(damage, weaknesses), resistances)
        if (realDamage < 0) realDamage = 0
        actualHp = actualHp - realDamage
        if (actualHp < 0) actualHp = 0
      }
    }

    override def isKO: Boolean = actualHp == 0

    override def removeFirstNEnergies(nEnergies: Int): Unit = nEnergies match {
      case n if n > 0 => removeEnergy(energiesMap.head._1); removeFirstNEnergies(nEnergies - 1)
      case _ =>
    }

    override def isBase: Boolean = evolutionName == ""

    override def clonePokemonCard: PokemonCard = copy(id, imageNumber, belongingSet, belongingSetCode, rarity, pokemonTypes, name,
      initialHp, actualHp, weaknesses, resistances, retreatCost, evolutionName, attacks, damageModifier, immune, status, energiesMap)
  }
}
