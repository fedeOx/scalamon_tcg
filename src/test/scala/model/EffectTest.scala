package model

import model.Cards.EnergyCard.EnergyCardImpl
import model.Cards.{Card, EnergyCard, PokemonCard}
import model.EnergyType.EnergyType
import model.exception.MissingEnergyException
import org.scalatest.{FlatSpec, GivenWhenThen}

import scala.util.Random

class EffectTest extends FlatSpec with GivenWhenThen {

  val cardList: Seq[Card] = DataLoader.loadData(SetType.base)
    .filter(c => c.isInstanceOf[Card])
  val pokemonCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[PokemonCard])
  val energyCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[EnergyCard])
  var activePokemon: PokemonCard = _
  var enemyPokemon: PokemonCard = _
  var enemyBench: Seq[PokemonCard] = Seq()
  var myBench: Seq[PokemonCard] = Seq()

  behavior of "Pokemons Effect"

  it should "damage the enemy based on my water energies limited by 2 " in {
    Given(" a pokemon with damage based on its assigned energy")
    activePokemon = getSpecificPokemon("Blastoise")
    And(" a pokemon with water weakness ")
    enemyPokemon = getSpecificPokemon("Charizard")
    When("we add 4 energies to the atker")
    addEnergiesToPokemon(EnergyType.water, 4, activePokemon)
    And("atker do the attack")
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    Then("pokemon with water weakness will take double damage")
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 100)

    When("the defending pokemon has no water weakness")
    enemyPokemon = getSpecificPokemon("Gyarados")
    And("atker do the attack")
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    Then("the defending pokemon will take normal dmg based on water energies")
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 50)

    enemyPokemon.actualHp = enemyPokemon.initialHp
    When("i add two more energies to the attacking pokemon")
    addEnergiesToPokemon(EnergyType.water, 2, activePokemon)
    Then("having the attack limited to 2, the attacking pokemon will do maximum 60 damage")
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 60)


  }

  it should "damage the opposing pokemon according to its energy" in {
    Given(" a pokemon whose damage depends on the energy assigned to the opponent")
    activePokemon = getSpecificPokemon("Mewtwo")
    And("an opponent with 4 energies allotted")
    enemyPokemon = getSpecificPokemon("Pikachu")
    addEnergiesToPokemon(EnergyType.lightning, 4, enemyPokemon)
    Then("the attack must take away from the opposing pokemon 10 + 10 for each energy assigned to it")
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 10 * enemyPokemon.totalEnergiesStored)
  }

  it should "damage itself by value and all pokemon on the opposing bench" in {
    Given(" a pokemon with auto-damage and multi-damage atk")
    activePokemon = getSpecificPokemon("Magneton")
    enemyPokemon = getSpecificPokemon("Venusaur")
    enemyBench = enemyBench :+ getSpecificPokemon("Alakazam")
    enemyBench = enemyBench :+ getSpecificPokemon("Squirtle")
    enemyBench = enemyBench :+ getSpecificPokemon("Abra")
    myBench = myBench :+ getSpecificPokemon("Magikarp")
    myBench = myBench :+ getSpecificPokemon("Charmeleon")

    When("atker do attack")
    activePokemon.attacks(1).effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    Then(" dmg enemy pokemon by 80")
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 80)
    Then("it dmg itself")
    assert(activePokemon.actualHp == activePokemon.initialHp - activePokemon.attacks(1).damage.get)
    And("damage all the enemy benched pokemon by 20")
    assert(enemyBench.head.actualHp == enemyBench.head.initialHp - 20)
    assert(enemyBench(1).actualHp == enemyBench(1).initialHp - 20)
    assert(enemyBench(2).actualHp == enemyBench(2).initialHp - 20)
    And("damage all my benched pokemon by 20")
    assert(myBench.head.actualHp == myBench.head.initialHp - 20)
    assert(myBench(1).actualHp == myBench(1).initialHp - 20)

  }

  it should "damage the opposing pokemon and damage himself if tail" in {
    Given("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Zapdos")
    enemyPokemon = getSpecificPokemon("Beedrill")
    Then("apply effect")
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    assert((activePokemon.actualHp == activePokemon.initialHp - 30 && enemyPokemon.actualHp == enemyPokemon.initialHp - 60) || (activePokemon.actualHp == activePokemon.initialHp && enemyPokemon.actualHp == enemyPokemon.initialHp - 60))
  }

  it should "damage according to the number of heads " in {
    Given("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Beedrill")
    enemyPokemon = getSpecificPokemon("Dragonair")
    Then("apply effect")
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 30 || enemyPokemon.actualHp == enemyPokemon.initialHp - 60 || enemyPokemon.actualHp == enemyPokemon.initialHp)

  }

  it should "damage and discard 2 energy " in {
    Given("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Charizard")
    addEnergiesToPokemon(EnergyType.fire, 2, activePokemon)
    enemyPokemon = getSpecificPokemon("Machamp")
    val initalEnergies = activePokemon.totalEnergiesStored
    Then("apply effect")
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 100)
    And("the attacking pokemon must have 2 less energy")
    assert(activePokemon.totalEnergiesStored == initalEnergies - 2)

  }

  it should "discard 1 psychic energy and setImmunity " in {
    Given("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Mewtwo")
    enemyPokemon = getSpecificPokemon("Machamp")
    addEnergiesToPokemon(EnergyType.psychic, 2, activePokemon)
    Then("apply effect")
    And("the attacking pokemon must have 1 less psychic energy")
    assert(activePokemon.hasEnergies(Seq(EnergyType.psychic, EnergyType.psychic)))
    assert(!activePokemon.immune)
    activePokemon.attacks(1).effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    assert(activePokemon.immune)
    assert(activePokemon.hasEnergies(Seq(EnergyType.psychic)))
  }

  it should "discard 1 psychic energy and recover all life " in {
    Given("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Kadabra")
    activePokemon.actualHp = 10
    addEnergiesToPokemon(EnergyType.psychic, 2, activePokemon)
    Then("apply effect")
    And("the attacking pokemon must have 1 less psychic energy")
    assert(activePokemon.hasEnergies(Seq(EnergyType.psychic, EnergyType.psychic)))
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    And("have all Hp")
    assert(activePokemon.actualHp == activePokemon.initialHp)
  }

  it should "must damage the enemy pokemon, subtracting from 50, 10 for each damage received" in {
    Given("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Machoke")
    activePokemon.actualHp = 60
    Then("apply effect")
    And("the attacking pokemon must do 30 Dmg")
    enemyPokemon.actualHp = enemyPokemon.initialHp
    activePokemon.attacks.head.effect.get.useEffect(enemyBench, myBench, activePokemon, enemyPokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp-30)
  }

  def getSpecificPokemon(_name: String): PokemonCard = {
    val pokemonCard = pokemonCards.find(pkm => pkm.asInstanceOf[PokemonCard].name == _name).get.asInstanceOf[PokemonCard]
    pokemonCard
  }

  def addEnergiesToPokemon(energyType: EnergyType, numberOfEnergy: Int, pokemon: PokemonCard): Unit = {
    for (i <- 1 to numberOfEnergy)
      pokemon.addEnergy(energyCards.find(energy => energy.asInstanceOf[EnergyCard].energyType == energyType).get.asInstanceOf[EnergyCard])
  }


}
