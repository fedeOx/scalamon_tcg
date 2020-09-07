package model

import model.Cards.{EnergyCard, PokemonCard}
import model.exception.MissingEnergyException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.GivenWhenThen

class CardTest extends AnyFlatSpec with GivenWhenThen {
  val pokemonCardList: Seq[PokemonCard] = DataLoader.loadData(SetType.base)
    .filter(c => c.isInstanceOf[PokemonCard])
    .map(c => c.asInstanceOf[PokemonCard])

  val pokemonCard: PokemonCard = pokemonCardList.head

  behavior of "A PokemonCard"

  it should "not have any EnergyCard assigned initially" in {
    assert(pokemonCard.totalEnergiesStored == 0)
  }

  it should "throw MissingEnergyException trying to remove a non-assigned EnergyCard" in {
    intercept[MissingEnergyException] {
      pokemonCard.removeEnergy(EnergyType.grass)
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
    otherPokemonCard.addDamage(damage, pokemonCard.pokemonTypes ++ Seq(EnergyType.grass))

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

  behavior of "A EnergyCard"

  val energyCardList: Seq[EnergyCard] = DataLoader.loadData(SetType.base)
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
