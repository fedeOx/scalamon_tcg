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
  var enemyPokemon : PokemonCard = _
  var pokemonBench: Seq[PokemonCard] = Seq()

  behavior of "Pokemons Effect"

  it should "damage the enemy based on my water energies limited by 2 " in {
    Given(" a pokemon with damage based on its assigned energy")
    activePokemon = getSpecificPokemon("Blastoise")
    And(" a pokemon with water weakness ")
    enemyPokemon = getSpecificPokemon("Charizard")
    When("we add 4 energies to the atker")
    addEnergiesToPokemon(EnergyType.water,3,activePokemon)
    And("atker do the attack")
    activePokemon.attacks.head.effect.get.useEffect(Seq(enemyPokemon),activePokemon)
    Then("pokemon with water weakness will take double damage")
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp-100)

    When("the defending pokemon has no water weakness")
    enemyPokemon = getSpecificPokemon("Gyarados")
    And("atker do the attack")
    activePokemon.attacks.head.effect.get.useEffect(Seq(enemyPokemon),activePokemon)
    Then("the defending pokemon will take normal dmg based on water energies")
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp-50)

    enemyPokemon.actualHp=enemyPokemon.initialHp
    When("i add two more energies to the attacking pokemon")
    addEnergiesToPokemon(EnergyType.water,2,activePokemon)
    Then("having the attack limited to 2, the attacking pokemon will do maximum 60 damage")
    activePokemon.attacks.head.effect.get.useEffect(Seq(enemyPokemon),activePokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp-60)



  }

  it should "damage the opposing pokemon according to its energy" in {
    Given(" a pokemon whose damage depends on the energy assigned to the opponent")
    activePokemon = getSpecificPokemon("Mewtwo")
    And("an opponent with 3 energies allotted")
    enemyPokemon = getSpecificPokemon("Pikachu")
    addEnergiesToPokemon(EnergyType.lightning,3,enemyPokemon)
    Then("the attack must take away from the opposing pokemon 10 + 10 for each energy assigned to it")
    activePokemon.attacks.head.effect.get.useEffect(Seq(enemyPokemon),activePokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp - 10 * enemyPokemon.totalEnergiesStored)
  }

  it should "damage itself by value and all pokemon on the opposing bench" in {
    Given(" a pokemon with auto-damage and multi-damage atk")
    activePokemon = getSpecificPokemon("Magneton")
    enemyPokemon = getSpecificPokemon("Venusaur")
    pokemonBench = pokemonBench :+ getSpecificPokemon("Alakazam")
    pokemonBench = pokemonBench :+ getSpecificPokemon("Kadabra")
    pokemonBench = pokemonBench :+ getSpecificPokemon("Abra")
    When("atker do attack")
    activePokemon.attacks(1).effect.get.useEffect(pokemonBench,activePokemon)
    Then("it dmg itself")
    assert(activePokemon.actualHp == activePokemon.initialHp - activePokemon.attacks(1).damage.get)
    And("damage all benched pokemon by 20")
    assert(pokemonBench.head.actualHp == pokemonBench.head.initialHp - 20)
    assert(pokemonBench(1).actualHp == pokemonBench(1).initialHp - 20)
    assert(pokemonBench(2).actualHp == pokemonBench(2).initialHp - 20)



  }

  it should "damage the opposing pokemon and damage himself if tail" in {
    Given ("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Zapdos")
    enemyPokemon = getSpecificPokemon("Beedrill")
    Then("apply effect")
    activePokemon.attacks.head.effect.get.useEffect(Seq(enemyPokemon),activePokemon)
    assert((activePokemon.actualHp == activePokemon.initialHp-30 && enemyPokemon.actualHp == enemyPokemon.initialHp-60) || (activePokemon.actualHp == activePokemon.initialHp && enemyPokemon.actualHp == enemyPokemon.initialHp-60))
  }

  it should "damage according to the number of heads " in {
    Given ("a pokemon with this effect")
    activePokemon = getSpecificPokemon("Beedrill")
    enemyPokemon = getSpecificPokemon("Dragonair")
    Then("apply effect")
    activePokemon.attacks.head.effect.get.useEffect(Seq(enemyPokemon),activePokemon)
    assert(enemyPokemon.actualHp == enemyPokemon.initialHp-30 || enemyPokemon.actualHp == enemyPokemon.initialHp-60 || enemyPokemon.actualHp == enemyPokemon.initialHp)

  }


  def getSpecificPokemon(_name: String): PokemonCard = {
    val pokemonCard  = pokemonCards.find(pkm => pkm.asInstanceOf[PokemonCard].name == _name).get.asInstanceOf[PokemonCard]
    pokemonCard
  }
  def addEnergiesToPokemon(energyType: EnergyType,numberOfEnergy : Int,pokemon : PokemonCard): Unit =
  {
      for(i <- 0 to numberOfEnergy)
        pokemon.addEnergy(energyCards.find(energy => energy.asInstanceOf[EnergyCard].energyType == energyType).get.asInstanceOf[EnergyCard])
  }




}
