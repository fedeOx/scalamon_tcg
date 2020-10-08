package model.game

import model.core.DataLoader
import model.exception.InvalidOperationException
import model.game.Cards.{EnergyCard, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec

class CardTest extends AnyFlatSpec with GivenWhenThen {

  val dataLoader: DataLoader = DataLoader()
  val pokemonCard: PokemonCard = getPokemon(SetType.Base, "Pikachu").get

  behavior of "A PokemonCard"

  it should "not have any EnergyCard assigned initially" in {
    assert(pokemonCard.totalEnergiesStored == 0)
  }

  it should "throw MissingEnergyException trying to remove a non-assigned EnergyCard" in {
    intercept[InvalidOperationException] {
      pokemonCard.removeEnergy(EnergyType.Grass)
    }
  }

  it should "be damaged if another PokemonCard attacks it" in {
    val damage = 20
    pokemonCard.addDamage(damage, Seq())
    assert(pokemonCard.actualHp == pokemonCard.initialHp - damage)
    pokemonCard.addDamage(damage, Seq())
    assert(pokemonCard.actualHp == pokemonCard.initialHp - damage - damage)

    pokemonCard.actualHp = pokemonCard.initialHp
    Given("a PokemonCard whose EnergyType is a Weakness of the other")
    var otherPokemonCard: PokemonCard = getPokemon(SetType.Base, "Blastoise").get

    When("it attacks the other")
    otherPokemonCard.addDamage(damage, pokemonCard.pokemonTypes)

    Then("the other is damaged twice")
    assert(otherPokemonCard.actualHp == otherPokemonCard.initialHp - damage*2)

    pokemonCard.actualHp = pokemonCard.initialHp
    Given( "a PokemonCard whose EnergyType is a Resistance of the other")
    otherPokemonCard = getPokemon(SetType.Base, "Sandshrew").get

    When("it attacks the other")
    otherPokemonCard.addDamage(damage, pokemonCard.pokemonTypes)

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

  val energyCard1: EnergyCard = getEnergy(SetType.Base, EnergyType.Grass).get
  val energyCard2: EnergyCard = getEnergy(SetType.Base, EnergyType.Fire).get

  it can "be added to a PokemonCard" in {
    assert(energyCard1.isBasic)
    pokemonCard.addEnergy(energyCard1)
    assert(pokemonCard.totalEnergiesStored == 1)
    assert(pokemonCard.hasEnergies(Seq(energyCard1.energyType)))
    assert(!pokemonCard.hasEnergies(Seq(energyCard2.energyType)))
  }

  it can "be added to a PokemonCard multiple times" in  {
    pokemonCard.addEnergy(energyCard1)
    pokemonCard.addEnergy(energyCard1)
    assert(pokemonCard.totalEnergiesStored == 3)
    assert(pokemonCard.hasEnergies(Seq(energyCard1.energyType, energyCard1.energyType, energyCard1.energyType)))
  }

  it can "be removed from a PokemonCard" in {
    pokemonCard.removeEnergy(energyCard1.energyType)
    assert(pokemonCard.totalEnergiesStored == 2)
    pokemonCard.removeEnergy(energyCard1.energyType)
    pokemonCard.removeEnergy(energyCard1.energyType)
    assert(pokemonCard.totalEnergiesStored == 0)
    assert(!pokemonCard.hasEnergies(Seq(energyCard1.energyType)))
    assert(!pokemonCard.hasEnergies(Seq(energyCard2.energyType)))
  }

  behavior of "A special EnergyCard"

  val specialEnergyCard: EnergyCard = getEnergy(SetType.Base, EnergyType.Colorless).get

  it should "be evaluated twice when added to a PokemonCard" in {
    assert(!specialEnergyCard.isBasic)
    assert(specialEnergyCard.energiesProvided == 2)
    pokemonCard.addEnergy(specialEnergyCard)
    assert(pokemonCard.totalEnergiesStored == 2)
    assert(pokemonCard.hasEnergies(Seq(specialEnergyCard.energyType, specialEnergyCard.energyType)))
    assert(!pokemonCard.hasEnergies(Seq(energyCard2.energyType)))
  }

  private def getPokemon(set: SetType, pokemonName: String): Option[PokemonCard] =
    dataLoader.loadSet(set).filter(p => p.isInstanceOf[PokemonCard] && p.asInstanceOf[PokemonCard].name == pokemonName)
      .map(p => p.asInstanceOf[PokemonCard]).headOption

  private def getEnergy(set: SetType, energyType: EnergyType): Option[EnergyCard] =
    dataLoader.loadSet(set).filter(p => p.isInstanceOf[EnergyCard] && p.asInstanceOf[EnergyCard].energyType == energyType)
      .map(p => p.asInstanceOf[EnergyCard]).headOption
}
