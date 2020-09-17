package model.game

import model.core.DataLoader
import model.exception.MissingEnergyException
import model.game.Cards.{EnergyCard, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.Weakness.Operation
import model.game.Weakness.Operation.Operation
import org.scalatest.{GivenWhenThen, OneInstancePerTest}
import org.scalatest.flatspec.AnyFlatSpec

class CardTest extends AnyFlatSpec with GivenWhenThen {
  val pokemonCardList: Seq[PokemonCard] = DataLoader.loadSet(SetType.Base)
    .filter(c => c.isInstanceOf[PokemonCard])
    .map(c => c.asInstanceOf[PokemonCard])

  val pokemonCard: PokemonCard = pokemonCardList.head

  behavior of "A PokemonCard"

  it should "not have any EnergyCard assigned initially" in {
    assert(pokemonCard.totalEnergiesStored == 0)
  }

  it should "throw MissingEnergyException trying to remove a non-assigned EnergyCard" in {
    intercept[MissingEnergyException] {
      pokemonCard.removeEnergy(EnergyType.Grass)
    }
  }

  it should "be damaged if another PokemonCard attacks it" in {
    val damage = 20
    pokemonCard.addDamage(damage, Seq())
    assert(pokemonCard.actualHp == pokemonCard.initialHp - damage)
    pokemonCard.addDamage(damage, Seq())
    assert(pokemonCard.actualHp == pokemonCard.initialHp - damage - damage)

    Given("a PokemonCard whose EnergyType is a Weakness of the other")
    var otherPokemonCard = pokemonCardList(6)

    When("it attacks the other")
    otherPokemonCard.addDamage(damage, pokemonCard.pokemonTypes)

    Then("the other is damaged twice")
    assert(otherPokemonCard.actualHp == otherPokemonCard.initialHp - damage*2)

    Given( "a PokemonCard whose EnergyType is a Resistance of the other")
    otherPokemonCard = pokemonCardList(2)

    When("it attacks the other")
    otherPokemonCard.addDamage(damage, pokemonCard.pokemonTypes ++ Seq(EnergyType.Grass))

    Then("the damage is reduced (-30 points)")
    var expectedHp = otherPokemonCard.initialHp - (damage - 30)
    if (expectedHp > otherPokemonCard.initialHp)
      expectedHp = otherPokemonCard.initialHp
    assert(otherPokemonCard.actualHp == expectedHp)
  }

  it should "become KO if its actual HP becomes 0" in {
    val aLittleDamage = 15
    while (pokemonCard.actualHp > 0) {
      pokemonCard.addDamage(aLittleDamage, Seq())
    }
    assert(pokemonCard.isKO)
    pokemonCard.addDamage(aLittleDamage, Seq())
    assert(pokemonCard.isKO)
  }

  val weakness: Weakness = new Weakness {
    override def energyType: EnergyType = EnergyType.Fighting
    override def operation: Operation = Operation.multiply2
  }
  val resistance: Resistance = new Resistance {
    override def energyType: EnergyType = EnergyType.Lightning
    override def reduction: Int = 30
  }
  val actualPokemonCard: PokemonCard = PokemonCard("123", "base1", Seq(EnergyType.Colorless), "pokemonName", 100, Seq(weakness),
    Seq(resistance), Seq(EnergyType.Colorless, EnergyType.Colorless), "", Nil)

  it should "be damaged twice if the attacker has EnergyType that is a Weakness for the actual PokemonCard" in {
    actualPokemonCard.addDamage(20, Seq(EnergyType.Fighting))
    assert(actualPokemonCard.actualHp == 60)
    actualPokemonCard.addDamage(40, Seq(EnergyType.Fighting))
    assert(actualPokemonCard.actualHp == 0)
  }

  it should "be damaged less (-30 points) if the attacker has EnergyType that is a Resistance for the actual PokemonCard" in {
    actualPokemonCard.actualHp = 100 // reset hp
    actualPokemonCard.addDamage(20, Seq(EnergyType.Lightning))
    assert(actualPokemonCard.actualHp == 100)
    actualPokemonCard.addDamage(30, Seq(EnergyType.Lightning))
    assert(actualPokemonCard.actualHp == 100)
    actualPokemonCard.addDamage(40, Seq(EnergyType.Lightning))
    assert(actualPokemonCard.actualHp == 90)
  }

  behavior of "A EnergyCard"

  val energyCardList: Seq[EnergyCard] = DataLoader.loadSet(SetType.Base)
    .filter(c => c.isInstanceOf[EnergyCard])
    .map(c => c.asInstanceOf[EnergyCard])
  val energyCard: EnergyCard = energyCardList(1)
  val differentEnergyCard: EnergyCard = energyCardList(2)

  it can "be added to a PokemonCard" in {
    assert(energyCard.isBasic)
    pokemonCard.addEnergy(energyCard)
    assert(pokemonCard.totalEnergiesStored == 1)
    assert(pokemonCard.hasEnergies(Seq(energyCard.energyType)))
    assert(!pokemonCard.hasEnergies(Seq(differentEnergyCard.energyType)))
  }

  it can "be added to a PokemonCard multiple times" in  {
    pokemonCard.addEnergy(energyCard)
    pokemonCard.addEnergy(energyCard)
    assert(pokemonCard.totalEnergiesStored == 3)
    assert(pokemonCard.hasEnergies(Seq(energyCard.energyType, energyCard.energyType, energyCard.energyType)))
  }

  it can "be removed from a PokemonCard" in {
    pokemonCard.removeEnergy(energyCard.energyType)
    assert(pokemonCard.totalEnergiesStored == 2)
    pokemonCard.removeEnergy(energyCard.energyType)
    pokemonCard.removeEnergy(energyCard.energyType)
    assert(pokemonCard.totalEnergiesStored == 0)
    assert(!pokemonCard.hasEnergies(Seq(energyCard.energyType)))
    assert(!pokemonCard.hasEnergies(Seq(differentEnergyCard.energyType)))
  }

  behavior of "A special EnergyCard"

  val specialEnergyCard: EnergyCard = energyCardList.head
  val anotherPokemonCard: PokemonCard = pokemonCardList(1)

  it should "be evaluated twice when added to a PokemonCard" in {
    assert(!specialEnergyCard.isBasic)
    assert(specialEnergyCard.energiesProvided == 2)
    anotherPokemonCard.addEnergy(specialEnergyCard)
    assert(anotherPokemonCard.totalEnergiesStored == 2)
    assert(anotherPokemonCard.hasEnergies(Seq(specialEnergyCard.energyType, specialEnergyCard.energyType)))
    assert(!anotherPokemonCard.hasEnergies(Seq(differentEnergyCard.energyType)))
  }

}
