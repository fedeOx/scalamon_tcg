package model.ai

import model.card.{Card, EnergyCard, PokemonCard}
import model.core.{DataLoader, GameManager, TurnManager}
import model.game.{Board, EnergyType, SetType, StatusType}
import model.game.EnergyType.EnergyType
import org.scalatest.flatspec.AnyFlatSpec

object BoardTmp {
  var iaBoard: Board = Board(Seq())
  var playerBoard: Board = Board(Seq())
}

import model.game.Board
import org.scalatest.GivenWhenThen

class aiLogicManagerTest() extends AnyFlatSpec with GivenWhenThen {
  val gameManager: GameManager = GameManager()
  val turnManager: TurnManager = TurnManager()
  val cardList: Seq[Card] = DataLoader().loadSet(SetType.Base) ++ DataLoader().loadSet(SetType.Fossil)
  val pokemonCards: Seq[Card] = cardList.filter(p => p.isInstanceOf[PokemonCard])
  val energyCards: Seq[EnergyCard] = cardList.filter(p => p.isInstanceOf[EnergyCard]).asInstanceOf[Seq[EnergyCard]]
  turnManager.flipACoin()
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
    AiLogicManager.dmgToBench(2,20,BoardTmp.iaBoard)
    Then("pokemon on the bench must have 20 damage")
    assert(BoardTmp.iaBoard.pokemonBench.count(c => c.isDefined && c.get.actualHp == c.get.initialHp-20) == 2)
  }

  it should "evolve all pokemon on the field" in {
    AiLogicManager.doTurn(BoardTmp.iaBoard,BoardTmp.playerBoard,gameManager,turnManager)
    Then("active pokemon and bench Squirtle must be evolved")
    assert(BoardTmp.iaBoard.activePokemon.get.name == "Charmeleon")
    assert(BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).count(pkm => pkm.get.name == "Wartortle" ) == 1)
    assert(BoardTmp.iaBoard.pokemonBench.filter(c => c.isDefined).count(pkm => pkm.get.name == "Charmander" ) == 1)
    assert(BoardTmp.iaBoard.hand.count(pkm => pkm.name == "Charmeleon") == 0)
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
