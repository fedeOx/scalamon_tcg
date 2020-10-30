package model.ai

import model.card.{Card, EnergyCard, PokemonCard}
import model.core.{DataLoader, GameManager, TurnManager}
import model.game.{Board, EnergyType, SetType}
import model.game.EnergyType.EnergyType
import org.scalatest.flatspec.AnyFlatSpec

object BoardTmp {
  var iaBoard: Board = Board(Seq())
  var playerBoard: Board = Board(Seq())
}

import org.scalatest.GivenWhenThen
class aiLogicManagerTest() extends AnyFlatSpec with GivenWhenThen {
  val gameManager: GameManager = GameManager()
  val turnManager: TurnManager = TurnManager()
  val cardList: Seq[Card] = DataLoader().loadSet(SetType.Base) ++ DataLoader().loadSet(SetType.Fossil)
  val pokemonCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[PokemonCard])
  val energyCards: Seq[EnergyCard] = cardList.filter(p => p.isInstanceOf[EnergyCard]).asInstanceOf[Seq[EnergyCard]]
  turnManager.flipACoin()
  BoardTmp.playerBoard.activePokemon = Some(getSpecificPokemon("Growlithe"))
  behavior of "Pokemons Effect"

  it should "should place the heaviest pokemon as active" in {
    Given("a set of pokemon in hand")
    var hand: Seq[Card] = Seq()
    hand = hand :+ getSpecificPokemon("Squirtle")
    hand = hand :+ getSpecificPokemon("Wartortle")
    hand = hand :+ getSpecificPokemon("Charmander")
    hand = hand :+ getSpecificPokemon("Charmander")
    hand = hand :+ getSpecificPokemon("Charmeleon")
    hand = hand :+ getSpecificPokemon("Weedle")
    And("fire energies")
    hand = hand :+ energyCards.find(energy => energy.energyType == EnergyType.Fire).get
    hand = hand :+ energyCards.find(energy => energy.energyType == EnergyType.Fire).get
    BoardTmp.iaBoard.addCardsToHand(hand)
    AiLogicManager.placeCards(hand, BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    Then("among the pokemon that have evolution in hand, I will choose charmander as it will also have fire energies in hand")
    assert(BoardTmp.iaBoard.activePokemon.get.name == "Charmander")
    assert(BoardTmp.iaBoard.pokemonBench.count(c => c.isDefined) == 3)
  }

  it should "choose a certain number of pokemon and do damage on them" in {
    Given("given a set of pokemon on the bench")
    AiLogicManager.dmgToBench(2, 20, BoardTmp.iaBoard)
    Then("pokemon on the bench must have 20 damage")
    assert(BoardTmp.iaBoard.pokemonBench.count(c => c.isDefined && c.get.actualHp == c.get.initialHp - 20) == 2)
  }

  it should "evolve all pokemon on the field" in {
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    Then("active pokemon and bench Squirtle must be evolved")
    assert(BoardTmp.iaBoard.activePokemon.get.name == "Charmeleon")
    assert(BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).count(pkm => pkm.get.name == "Wartortle") == 1)
    assert(BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).count(pkm => pkm.get.name == "Charmander") == 1)
    assert(BoardTmp.iaBoard.hand.count(pkm => pkm.name == "Charmeleon") == 0)
  }

  it should "add two water energies to the hand and the evolution of Wartortle, so as to make the change with the active pokemon" in {
    addEnergiesToPokemon(EnergyType.Water, 3, BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).find(pkm => pkm.get.name == "Wartortle").flatten.get)
    BoardTmp.iaBoard.addCardsToHand(Seq(getSpecificPokemon("Blastoise"), energyCards.find(energy => energy.energyType == EnergyType.Water).get, energyCards.find(energy => energy.energyType == EnergyType.Water).get))
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    Then("Blastoise must be the active pokemon and charmeleon on the bench")
    assert(BoardTmp.iaBoard.activePokemon.get.name == "Blastoise")
    assert(BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).count(pkm => pkm.get.name == "Charmeleon") == 1)
    assert(BoardTmp.iaBoard.hand.count(pkm => pkm.name == "Blastoise") == 0)
  }

  it should "assign pokemon energies" in {
    val fireEnergy = energyCards.find(energy => energy.energyType == EnergyType.Fire).get
    BoardTmp.iaBoard.addCardsToHand(Seq(energyCards.find(energy => energy.energyType == EnergyType.Water).get.cloneEnergyCard))
    BoardTmp.iaBoard.activePokemon.get.removeFirstNEnergies(1)
    Given("the energies water in hand, I assign one to the active pokemon")
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    assert(BoardTmp.iaBoard.activePokemon.get.totalEnergiesStored == 3)

    And("assign an energy to the pokemon with the same type of energy in the hand")
    BoardTmp.iaBoard.addCardsToHand(Seq(fireEnergy.cloneEnergyCard))
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    assert(BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).count(pkm => pkm.get.name == "Charmeleon" && pkm.get.totalEnergiesStored == 2) == 1)

    And("assigns energies to Charmeleon until he can perform the second attack")
    BoardTmp.iaBoard.addCardsToHand(Seq(fireEnergy.cloneEnergyCard, fireEnergy.cloneEnergyCard, fireEnergy.cloneEnergyCard, fireEnergy.cloneEnergyCard, fireEnergy.cloneEnergyCard, fireEnergy.cloneEnergyCard))
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    assert(BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).count(pkm => pkm.get.name == "Charmeleon" && pkm.get.totalEnergiesStored == 3) == 1)

    Given("an active pokemon with only colorless attacks")
    BoardTmp.iaBoard.putPokemonInBenchPosition(Some(getSpecificPokemon("Vulpix")), 0)
    BoardTmp.iaBoard.putPokemonInBenchPosition(Some(getSpecificPokemon("Pikachu")), 1)
    BoardTmp.iaBoard.putPokemonInBenchPosition(Some(getSpecificPokemon("Vulpix")), 2)
    BoardTmp.iaBoard.putPokemonInBenchPosition(Some(getSpecificPokemon("Pikachu")), 3)
    BoardTmp.iaBoard.putPokemonInBenchPosition(Some(getSpecificPokemon("Pikachu")), 4)
    BoardTmp.iaBoard.activePokemon = Some(getSpecificPokemon("Chansey"))
    AiLogicManager.doTurn(BoardTmp.iaBoard, BoardTmp.playerBoard, gameManager, turnManager)
    Then("assign energy of any kind to the active pokemon")
    assert(BoardTmp.iaBoard.activePokemon.get.totalEnergiesStored == 1)
  }

  def getSpecificPokemon(_name: String): PokemonCard = {
    val pokemonCard: Option[PokemonCard] = pokemonCards.find(pkm => pkm.asInstanceOf[PokemonCard].name == _name).asInstanceOf[Option[PokemonCard]]
    PokemonCard(pokemonCard.get.id, pokemonCard.get.imageNumber, pokemonCard.get.belongingSet, pokemonCard.get.belongingSetCode, pokemonCard.get.rarity,
      pokemonCard.get.pokemonTypes, pokemonCard.get.name, pokemonCard.get.initialHp, pokemonCard.get.weaknesses, pokemonCard.get.resistances, pokemonCard.get.retreatCost,
      pokemonCard.get.evolutionName, pokemonCard.get.attacks)
  }

  def addEnergiesToPokemon(energyType: EnergyType, numberOfEnergy: Int, pokemon: PokemonCard): Unit = {
    for (i <- 1 to numberOfEnergy)
      pokemon.addEnergy(energyCards.find(energy => energy.energyType == energyType).get)
  }
}
