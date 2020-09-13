package model

import model.core.DataLoader
import model.game.Cards.EnergyCard.EnergyCardImpl
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.{EnergyType, SetType, StatusType}
import model.pokemonEffect.BoardTmp
import org.scalatest.{FlatSpec, GivenWhenThen}

import scala.util.Random

class EffectTest extends FlatSpec with GivenWhenThen {

  val cardList: Seq[Card] = DataLoader.loadSet(SetType.Base)
    .filter(c => c.isInstanceOf[Card])
  val pokemonCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[PokemonCard])
  val energyCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[EnergyCard])

  behavior of "Pokemons Effect"

  it should "damage the enemy based on my water energies limited by 2 " in {
    Given(" a pokemon with damage based on its assigned energy")
    BoardTmp.activePokemon  = getSpecificPokemon("Blastoise")
    And(" a pokemon with water weakness ")
    BoardTmp.defendingPokemon = getSpecificPokemon("Charizard")
    When("we add 4 energies to the atker")
    addEnergiesToPokemon(EnergyType.Water, 4, BoardTmp.activePokemon )
    And("atker do the attack")
    BoardTmp.activePokemon.attacks.head.effect.get.useEffect()
    Then("pokemon with water weakness will take double damage")
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 100)

    When("the defending pokemon has no water weakness")
    BoardTmp.defendingPokemon = getSpecificPokemon("Gyarados")
    And("atker do the attack")
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    Then("the defending pokemon will take normal dmg based on water energies")
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 50)
    BoardTmp.defendingPokemon.actualHp = BoardTmp.defendingPokemon.initialHp
    When("i add two more energies to the attacking pokemon")
    addEnergiesToPokemon(EnergyType.Water, 2, BoardTmp.activePokemon )
    Then("having the attack limited to 2, the attacking pokemon will do maximum 60 damage")
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 60)


  }

  it should "damage the opposing pokemon according to its energy" in {
    Given(" a pokemon whose damage depends on the energy assigned to the opponent")
    BoardTmp.activePokemon  = getSpecificPokemon("Mewtwo")
    And("an opponent with 4 energies allotted")
    BoardTmp.defendingPokemon = getSpecificPokemon("Pikachu")
    addEnergiesToPokemon(EnergyType.Lightning, 4, BoardTmp.defendingPokemon)
    Then("the attack must take away from the opposing pokemon 10 + 10 for each energy assigned to it")
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 10 * BoardTmp.defendingPokemon.totalEnergiesStored)
  }

  it should "damage itself by value and all pokemon on the opposing bench" in {
    Given(" a pokemon with auto-damage and multi-damage atk")
    BoardTmp.activePokemon  = getSpecificPokemon("Magneton")
    BoardTmp.defendingPokemon = getSpecificPokemon("Venusaur")
    BoardTmp.enemyBench = BoardTmp.enemyBench :+ getSpecificPokemon("Alakazam")
    BoardTmp.enemyBench = BoardTmp.enemyBench :+ getSpecificPokemon("Squirtle")
    BoardTmp.enemyBench = BoardTmp.enemyBench :+ getSpecificPokemon("Abra")
    BoardTmp.myBench = BoardTmp.myBench :+ getSpecificPokemon("Magikarp")
    BoardTmp.myBench = BoardTmp.myBench :+ getSpecificPokemon("Charmeleon")

    When("atker do attack")
    BoardTmp.activePokemon .attacks(1).effect.get.useEffect()
    Then(" dmg enemy pokemon by 80")
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 80)
    Then("it dmg itself")
    assert(BoardTmp.activePokemon .actualHp == BoardTmp.activePokemon .initialHp - BoardTmp.activePokemon .attacks(1).damage.get)
    And("damage all the enemy benched pokemon by 20")
    assert(BoardTmp.enemyBench.head.actualHp == BoardTmp.enemyBench.head.initialHp - 20)
    assert(BoardTmp.enemyBench(1).actualHp == BoardTmp.enemyBench(1).initialHp - 20)
    assert(BoardTmp.enemyBench(2).actualHp == BoardTmp.enemyBench(2).initialHp - 20)
    And("damage all my benched pokemon by 20")
    assert(BoardTmp.myBench.head.actualHp == BoardTmp.myBench.head.initialHp - 20)
    assert(BoardTmp.myBench(1).actualHp == BoardTmp.myBench(1).initialHp - 20)

  }

  it should "damage the opposing pokemon and damage himself if tail" in {
    Given("a pokemon with this effect")
    BoardTmp.activePokemon  = getSpecificPokemon("Zapdos")
    BoardTmp.defendingPokemon = getSpecificPokemon("Beedrill")
    Then("apply effect")
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    assert((BoardTmp.activePokemon .actualHp == BoardTmp.activePokemon .initialHp - 30 && BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 60) || (BoardTmp.activePokemon .actualHp == BoardTmp.activePokemon .initialHp && BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 60))
  }

  it should "damage according to the number of heads " in {
    Given("a pokemon with this effect")
    BoardTmp.activePokemon  = getSpecificPokemon("Beedrill")
    BoardTmp.defendingPokemon = getSpecificPokemon("Dragonair")
    Then("apply effect")
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 30 || BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 60 || BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp)

  }

  it should "damage and discard 2 energy " in {
    Given("a pokemon with this effect")
    BoardTmp.activePokemon  = getSpecificPokemon("Charizard")
    addEnergiesToPokemon(EnergyType.Fire, 2, BoardTmp.activePokemon )
    BoardTmp.defendingPokemon = getSpecificPokemon("Machamp")
    val initalEnergies = BoardTmp.activePokemon .totalEnergiesStored
    Then("apply effect")
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp - 100)
    And("the attacking pokemon must have 2 less energy")
    assert(BoardTmp.activePokemon .totalEnergiesStored == initalEnergies - 2)

  }

  it should "discard 1 psychic energy and setImmunity " in {
    Given("a pokemon with this effect")
    BoardTmp.activePokemon  = getSpecificPokemon("Mewtwo")
    BoardTmp.defendingPokemon = getSpecificPokemon("Machamp")
    addEnergiesToPokemon(EnergyType.Psychic, 2, BoardTmp.activePokemon )
    Then("apply effect")
    And("the attacking pokemon must have 1 less psychic energy")
    assert(BoardTmp.activePokemon .hasEnergies(Seq(EnergyType.Psychic, EnergyType.Psychic)))
    assert(!BoardTmp.activePokemon .immune)
    BoardTmp.activePokemon .attacks(1).effect.get.useEffect()
    assert(BoardTmp.activePokemon .immune)
    assert(BoardTmp.activePokemon .hasEnergies(Seq(EnergyType.Psychic)))
  }

  it should "discard 1 psychic energy and recover all life " in {
    Given("a pokemon with this effect")
    BoardTmp.activePokemon  = getSpecificPokemon("Kadabra")
    BoardTmp.activePokemon .actualHp = 10
    addEnergiesToPokemon(EnergyType.Psychic, 2, BoardTmp.activePokemon )
    Then("apply effect")
    And("the attacking pokemon must have 1 less psychic energy")
    assert(BoardTmp.activePokemon .hasEnergies(Seq(EnergyType.Psychic, EnergyType.Psychic)))
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    And("have all Hp")
    assert(BoardTmp.activePokemon .actualHp == BoardTmp.activePokemon .initialHp)
  }

  it should "must damage the enemy pokemon, subtracting from 50, 10 for each damage received" in {
    Given("a pokemon with this effect")
    BoardTmp.activePokemon  = getSpecificPokemon("Machoke")
    BoardTmp.activePokemon .actualHp = 60
    Then("apply effect")
    And("the attacking pokemon must do 30 Dmg")
    BoardTmp.defendingPokemon.actualHp = BoardTmp.defendingPokemon.initialHp
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    assert(BoardTmp.defendingPokemon.actualHp == BoardTmp.defendingPokemon.initialHp-30)
  }

  it should "confuse the enemy if head" in {
    Given("a pokemon with this effect")
    BoardTmp.activePokemon  = getSpecificPokemon("Alakazam")
    Then("apply effect")
    And("the attacking pokemon should confuse enemy")
    BoardTmp.activePokemon .attacks.head.effect.get.useEffect()
    assert(BoardTmp.defendingPokemon.status == StatusType.NoStatus || BoardTmp.defendingPokemon.status == StatusType.Confused)
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