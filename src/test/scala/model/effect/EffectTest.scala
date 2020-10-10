package model.effect


import controller.Controller
import model.core.{DataLoader, GameManager}
import model.game.{Board, EnergyType, SetType, StatusType}
import model.game.Cards._
import model.game.EnergyType.EnergyType
import org.scalatest.flatspec.AnyFlatSpec

object BoardTmp {
  var iaBoard: Board = Board(Seq())
  var playerBoard: Board = Board(Seq())
}

import org.scalatest.GivenWhenThen

class EffectTest() extends AnyFlatSpec with GivenWhenThen {

  val gameManager: GameManager = GameManager()
  val cardList: Seq[Card] = DataLoader().loadSet(SetType.Base) ++ DataLoader().loadSet(SetType.Fossil)
  val pokemonCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[PokemonCard])
  val energyCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[EnergyCard])

  behavior of "Pokemons Effect"


  it should "damage the enemy based on my water energies limited by 2 " in {
    Given(" a pokemon with damage based on its assigned energy")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Blastoise")
    And(" a pokemon with water weakness ")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Charizard")
    When("we add 4 energies to the atker")
    addEnergiesToPokemon(EnergyType.Water, 4, BoardTmp.iaBoard.activePokemon.get)
    And("atker do the attack")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    Then("pokemon with water weakness will take double damage")
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 100)

    When("the defending pokemon has no water weakness")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Gyarados")
    And("atker do the attack")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    Then("the defending pokemon will take normal dmg based on water energies")
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 50)
    BoardTmp.playerBoard.activePokemon.get.actualHp = BoardTmp.playerBoard.activePokemon.get.initialHp
    When("i add two more energies to the attacking pokemon")
    addEnergiesToPokemon(EnergyType.Water, 2, BoardTmp.iaBoard.activePokemon.get)
    Then("having the attack limited to 2, the attacking pokemon will do maximum 60 damage")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 60)
  }

  it should "damage the opposing pokemon according to its energy" in {
    Given(" a pokemon whose damage depends on the energy assigned to the opponent")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Mewtwo")
    And("an opponent with 4 energies allotted")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Pikachu")
    addEnergiesToPokemon(EnergyType.Lightning, 4, BoardTmp.playerBoard.activePokemon.get)
    Then("the attack must take away from the opposing pokemon 10 + 10 for each energy assigned to it")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 10 * BoardTmp.playerBoard.activePokemon.get.totalEnergiesStored)
  }


  it should "damage itself by value and all pokemon on the opposing bench" in {
    Given(" a pokemon with auto-damage and multi-damage atk")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Magneton")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Venusaur")
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Alakazam"), 0)
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Squirtle"), 1)
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Abra"), 2)

    BoardTmp.iaBoard.putPokemonInBenchPosition(getSpecificPokemon("Magikarp"), 0)
    BoardTmp.iaBoard.putPokemonInBenchPosition(getSpecificPokemon("Charmeleon"), 1)

    When("atker do attack")
    BoardTmp.iaBoard.activePokemon.get.attacks(1).effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    Then(" dmg enemy pokemon by 80")
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 80)
    Then("it dmg itself")
    assert(BoardTmp.iaBoard.activePokemon.get.actualHp == 0)
    And("damage all the enemy benched pokemon by 20")
    assert(BoardTmp.playerBoard.pokemonBench.head.get.actualHp == BoardTmp.playerBoard.pokemonBench.head.get.initialHp - 20)
    assert(BoardTmp.playerBoard.pokemonBench(1).get.actualHp == BoardTmp.playerBoard.pokemonBench(1).get.initialHp - 20)
    assert(BoardTmp.playerBoard.pokemonBench(2).get.actualHp == BoardTmp.playerBoard.pokemonBench(2).get.initialHp - 20)
    And("damage all my benched pokemon by 20")
    assert(BoardTmp.iaBoard.pokemonBench.head.get.actualHp == BoardTmp.iaBoard.pokemonBench.head.get.initialHp - 20)
    assert(BoardTmp.iaBoard.pokemonBench(1).get.actualHp == BoardTmp.iaBoard.pokemonBench(1).get.initialHp - 20)

  }

  it should "damage the opposing pokemon and damage himself if tail" in {
    Given("a pokemon with this effect")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Zapdos")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Beedrill")
    Then("apply effect")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert((BoardTmp.iaBoard.activePokemon.get.actualHp == BoardTmp.iaBoard.activePokemon.get.initialHp - 30 && BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 60) || (BoardTmp.iaBoard.activePokemon.get.actualHp == BoardTmp.iaBoard.activePokemon.get.initialHp && BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 60))
  }

  it should "damage according to the number of heads " in {
    Given("a pokemon with this effect")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Beedrill")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Dragonair")
    Then("apply effect")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 30 || BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 60 || BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp)

  }

  it should "damage and discard 2 general energy " in {

    Given("a pokemon with this effect")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Charizard")
    addEnergiesToPokemon(EnergyType.Fire, 2, BoardTmp.iaBoard.activePokemon.get)
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Machamp")
    val initalEnergies = BoardTmp.iaBoard.activePokemon.get.totalEnergiesStored
    Then("apply effect")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 100)
    And("the attacking pokemon must have 2 less energy")
    assert(BoardTmp.iaBoard.activePokemon.get.totalEnergiesStored == initalEnergies - 2)

  }

  it should "damage and discard ALL energies" in {
    Given("a pokemon with this effect")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Zapdos")
    addEnergiesToPokemon(EnergyType.Lightning, 4, BoardTmp.iaBoard.activePokemon.get)
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Charizard")
    BoardTmp.playerBoard.activePokemon.get.actualHp = 120
    Then("apply effect")
    BoardTmp.iaBoard.activePokemon.get.attacks.last.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 100 && BoardTmp.iaBoard.activePokemon.get.totalEnergiesStored == 0)

  }

  it should "discard 1 psychic energy and setImmunity " in {
    Given("a pokemon with this effect")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Mewtwo")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Machamp")
    addEnergiesToPokemon(EnergyType.Psychic, 2, BoardTmp.iaBoard.activePokemon.get)
    Then("apply effect")
    And("the attacking pokemon must have 1 less psychic energy")
    assert(BoardTmp.iaBoard.activePokemon.get.hasEnergies(Seq(EnergyType.Psychic, EnergyType.Psychic)))
    assert(!BoardTmp.iaBoard.activePokemon.get.immune)
    BoardTmp.iaBoard.activePokemon.get.attacks(1).effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert(BoardTmp.iaBoard.activePokemon.get.immune)
    assert(BoardTmp.iaBoard.activePokemon.get.hasEnergies(Seq(EnergyType.Psychic)))
  }

  it should "discard 1 psychic energy and recover all life " in {
    Given("a pokemon with this effect")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Kadabra")
    BoardTmp.iaBoard.activePokemon.get.actualHp = 10
    addEnergiesToPokemon(EnergyType.Psychic, 2, BoardTmp.iaBoard.activePokemon.get)
    Then("apply effect")
    And("the attacking pokemon must have 1 less psychic energy")
    assert(BoardTmp.iaBoard.activePokemon.get.hasEnergies(Seq(EnergyType.Psychic, EnergyType.Psychic)))
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    And("have all Hp")
    assert(BoardTmp.iaBoard.activePokemon.get.actualHp == BoardTmp.iaBoard.activePokemon.get.initialHp)
  }

  it should "must damage the enemy pokemon, subtracting from 50, 10 for each damage received" in {
    Given("a pokemon with this effect")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Machoke")
    BoardTmp.iaBoard.activePokemon.get.actualHp = 60
    Then("apply effect")
    And("the attacking pokemon must do 30 Dmg")
    BoardTmp.playerBoard.activePokemon.get.actualHp = BoardTmp.playerBoard.activePokemon.get.initialHp
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 30)
  }

  it should "confuse the enemy if head" in {
    Given("a pokemon with this effect")
    BoardTmp.playerBoard.activePokemon.get.actualHp = BoardTmp.playerBoard.activePokemon.get.initialHp
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Vulpix")
    Then("apply effect")
    And("the attacking pokemon should confuse enemy")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    assert((BoardTmp.playerBoard.activePokemon.get.status == StatusType.NoStatus || BoardTmp.playerBoard.activePokemon.get.status == StatusType.Confused) && BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 10)
  }

  it should "damage opponent pkm  by value and all pokemon on the opposing bench or the atker bench" in {
    Given("Articuno")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Articuno")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Golbat")
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Dugtrio"), 0)
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Zapdos"), 1)
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Electrode"), 2)

    BoardTmp.iaBoard.putPokemonInBenchPosition(getSpecificPokemon("Dratini"), 0)
    BoardTmp.iaBoard.putPokemonInBenchPosition(getSpecificPokemon("Diglett"), 1)

    When("atker do attack")
    BoardTmp.iaBoard.activePokemon.get.attacks(1).effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    Then(" dmg enemy pokemon by 50")
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 50)
    And("damage all the enemy benched pokemon by 10 if head or my benched pokemon if tail")
    assert((BoardTmp.playerBoard.pokemonBench.head.get.actualHp == BoardTmp.playerBoard.pokemonBench.head.get.initialHp - 10 &&
      BoardTmp.playerBoard.pokemonBench(1).get.actualHp == BoardTmp.playerBoard.pokemonBench(1).get.initialHp - 10 &&
      BoardTmp.playerBoard.pokemonBench(2).get.actualHp == BoardTmp.playerBoard.pokemonBench(2).get.initialHp - 10) || (
      BoardTmp.iaBoard.pokemonBench.head.get.actualHp == BoardTmp.iaBoard.pokemonBench.head.get.initialHp - 10 &&
        BoardTmp.iaBoard.pokemonBench(1).get.actualHp == BoardTmp.iaBoard.pokemonBench(1).get.initialHp - 10))
  }

 /* it should "the pokemon must damage the defending pokemon and a pokemon on its bench" in {
    Given("gengar")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Gengar")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Tentacruel")
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Lapras"), 0)
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Golem"), 1)
    BoardTmp.playerBoard.putPokemonInBenchPosition(getSpecificPokemon("Graveler"), 2)
    When("atker do attack")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    Then(" dmg enemy pokemon by 30")
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == BoardTmp.playerBoard.activePokemon.get.initialHp - 30)
    And("damage one of the opposing bench pokemon by 10")
    assert(BoardTmp.playerBoard.pokemonBench.head.get.actualHp == BoardTmp.playerBoard.pokemonBench.head.get.initialHp - 10)
  }*/

  it should "the pokemon must recover 20 of life" in {
    Given("Golbat")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Golbat")
    BoardTmp.iaBoard.activePokemon.get.addDamage(20,Seq(EnergyType.Colorless))
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Tentacruel")
    When("atker do attack")
    BoardTmp.iaBoard.activePokemon.get.attacks(1).effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    Then(" dmg enemy pokemon by 20")
    assert(BoardTmp.playerBoard.activePokemon.get.actualHp == (BoardTmp.playerBoard.activePokemon.get.initialHp - 20))
    And("recover 20 hp")
    assert( BoardTmp.iaBoard.activePokemon.get.actualHp == BoardTmp.iaBoard.activePokemon.get.initialHp )
  }

  it should "the pokemon must not take damage on the next turn if less than 30" in {
    Given("Graveler")
    BoardTmp.iaBoard.activePokemon = getSpecificPokemon("Graveler")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Tentacruel")
    When("atker do attack")
    BoardTmp.iaBoard.activePokemon.get.attacks.head.effect.get.useEffect(BoardTmp.iaBoard, BoardTmp.playerBoard,gameManager)
    Then("Graveler must have the modifier field at 30")
    assert(BoardTmp.iaBoard.activePokemon.get.damageModifier == 30)
    And("Tentacruel do 10 dmg ")
    BoardTmp.playerBoard.activePokemon.get.attacks(1).effect.get.useEffect(BoardTmp.playerBoard, BoardTmp.iaBoard,gameManager)
    Then("Graveler must have all life")
    assert( BoardTmp.iaBoard.activePokemon.get.actualHp == BoardTmp.iaBoard.activePokemon.get.initialHp )
    And("Set Hitmonlee to do 50 dmg ")
    BoardTmp.playerBoard.activePokemon = getSpecificPokemon("Hitmonlee")
    BoardTmp.playerBoard.activePokemon.get.attacks(1).effect.get.useEffect(BoardTmp.playerBoard, BoardTmp.iaBoard,gameManager)
    Then("Graveler must lose 20 hp")
    assert( BoardTmp.iaBoard.activePokemon.get.actualHp == (BoardTmp.iaBoard.activePokemon.get.initialHp-20) )
  }




  def getSpecificPokemon(_name: String): Option[PokemonCard] = {
    val pokemonCard: Option[PokemonCard] = pokemonCards.find(pkm => pkm.asInstanceOf[PokemonCard].name == _name).asInstanceOf[Option[PokemonCard]]
    Some(PokemonCard(pokemonCard.get.id, pokemonCard.get.imageNumber, pokemonCard.get.belongingSet, pokemonCard.get.belongingSetCode, pokemonCard.get.rarity, pokemonCard.get.pokemonTypes, pokemonCard.get.name, pokemonCard.get.initialHp, pokemonCard.get.weaknesses, pokemonCard.get.resistances, pokemonCard.get.retreatCost, pokemonCard.get.evolutionName, pokemonCard.get.attacks))
  }

  def addEnergiesToPokemon(energyType: EnergyType, numberOfEnergy: Int, pokemon: PokemonCard): Unit = {
    for (i <- 1 to numberOfEnergy)
      pokemon.addEnergy(energyCards.find(energy => energy.asInstanceOf[EnergyCard].energyType == energyType).get.asInstanceOf[EnergyCard])
  }

}
