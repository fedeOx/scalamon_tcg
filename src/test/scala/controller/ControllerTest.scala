package controller

import common.{Observer, TurnOwner}
import common.TurnOwner.TurnOwner
import model.core.{DataLoader, GameManager, TurnManager}
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, FlipCoin, NextTurn, ShowDeckCards}
import model.exception.CoinNotLaunchedException
import model.game.Cards.EnergyCard.EnergyCardType
import model.game.Cards.{EnergyCard, PokemonCard}
import model.game.{Board, DeckCard, DeckType, EnergyType, SetType}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec

class ControllerTest extends AnyFlatSpec with MockFactory with GivenWhenThen {
  /*
  val controller: Controller = Controller()
  val observerMock: Observer = mock[Observer]
  DataLoader.addObserver(observerMock)
  GameManager.addObserver(observerMock)
  TurnManager.addObserver(observerMock)

  behavior of "A Controller"

  it must "make DataLoader notify observers when a new deck is loaded" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[ShowDeckCards]
      e.asInstanceOf[ShowDeckCards].deckCards.isInstanceOf[Seq[DeckCard]]
      e.asInstanceOf[ShowDeckCards].deckCards.nonEmpty
    }})
    controller.loadDeckCards(SetType.Base, DeckType.Base1)
    waitForControllerThread()
  }

  it should "make TurnManager launch a CoinNotLaunchedException if a player is ready before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      controller.playerReady()
    }
  }

  it should "make TurnManager launch a CoinNotLaunchedException when someone tries to switch turn before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      controller.endTurn()
    }
  }

  it must "make GameManager notify observers when the game field is ready" in {
    inAnyOrder {
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[BuildGameField]
        val event: BuildGameField = e.asInstanceOf[BuildGameField]
        event.playerBoard.isInstanceOf[Board]
        event.opponentBoard.isInstanceOf[Board]
        checkBoardCorrectness(event.playerBoard)
        checkBoardCorrectness(event.opponentBoard)
      }})
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[FlipCoin]
        val event: FlipCoin = e.asInstanceOf[FlipCoin]
        event.coinValue.isInstanceOf[TurnOwner]
        TurnOwner.values.contains(event.coinValue)
      }})
    }
    val deckCards: Seq[DeckCard] = DataLoader.loadDeck(SetType.Base, DeckType.Base1)
    controller.initGame(deckCards, SetType.Base)
    waitForControllerThread()
  }

  it must "make TurnManager notify observers when the user confirm to start the game" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[PlaceCards]
    }})
    controller.startGame()
    waitForControllerThread()
  }

  it must "make TurnManager notify observers when both human player and AI player are ready to play" in {
    inAnyOrder {
      (observerMock.update _).expects(where {e: Event => {
        e.isInstanceOf[NextTurn]
        e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
      }})
    }
    TurnManager.playerReady() // AI player is ready
    controller.playerReady() // Human player is ready
  }

  it should "make TurnManager notify observers when a player ends his turn" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurn]
      e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
    }})
    controller.endTurn()
  }

  it should "add and destroy active pokemon when it is possible" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }})
    assert(GameManager.isPlayerActivePokemonEmpty)
    val activePokemonToAdd: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    controller.addActivePokemon(activePokemonToAdd)

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).twice()
    Given("a new active pokemon to add")
    val newActivePokemonToAdd: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myNewActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    When("an active pokemon is already present")
    assert(!GameManager.isPlayerActivePokemonEmpty)
    Then("an ActivePokemonException should be thrown")
    intercept[ActivePokemonException] {
      controller.addActivePokemon(newActivePokemonToAdd)
    }
    When("the actual active pokemon is destroyed")
    controller.destroyActivePokemon()
    assert(GameManager.isPlayerActivePokemonEmpty)
    Then("the new active pokemon can be added")
    controller.addActivePokemon(newActivePokemonToAdd)
  }

  it should "add and remove pokemon from bench when it is possible" in {
    val BenchSize = 5
    for (i <- 0 until BenchSize) assert(GameManager.isPlayerBenchLocationEmpty(i))

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).repeat(BenchSize)
    val benchPokemonToAdd: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    for (i <- 0 until BenchSize) controller.addPokemonToBench(benchPokemonToAdd, i)
    for (i <- 0 until BenchSize) assert(!GameManager.isPlayerBenchLocationEmpty(i))

    Given("a new bench pokemon to add")
    val newBenchPokemonToAdd: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myNewBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    When("a pokemon is already present in the specified position of the bench")
    assert(!GameManager.isPlayerBenchLocationEmpty(0))
    Then("a BenchPokemonException should be thrown")
    intercept[BenchPokemonException] {
      controller.addPokemonToBench(newBenchPokemonToAdd, 0)
    }

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).repeat(BenchSize)
    for (i <- 0 until BenchSize) controller.destroyPokemonFromBench(i)
    for (i <- 0 until BenchSize) assert(GameManager.isPlayerBenchLocationEmpty(i))
  }

  it should "make GameManager notify observers when card is draw from deck or prize cards stack" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).twice()
    controller.drawACard()
    controller.drawAPrizeCard()
  }


  it should "manage the user game routines with active pokemon" in {
    val playerBoard = GameManager.playerBoard
    playerBoard.activePokemon = None

    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).anyNumberOfTimes()

    Given("a base pokemon card selected by the user from his hand")
    val basePokemonCard: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    controller.handCardSelected = Some(basePokemonCard)

    When("the active pokemon location is empty and selected by the user")
    assert(playerBoard.activePokemon.isEmpty)
    controller.selectActivePokemonLocation()

    Then("the base pokemon card selected by the user should be placed in active pokemon position")
    assert(playerBoard.activePokemon.nonEmpty)
    assert(playerBoard.activePokemon.get == basePokemonCard)

    Given("an energy card selected by the user from his hand")
    val energyCard = EnergyCard("1", "base1", EnergyType.Grass, EnergyCardType.basic)
    controller.handCardSelected = Some(energyCard)

    When("the active pokemon location is not empty and selected by the user")
    assert(playerBoard.activePokemon.nonEmpty)
    controller.selectActivePokemonLocation()

    Then("the energy card selected by the user should be assigned to the active pokemon")
    assert(playerBoard.activePokemon.nonEmpty)
    assert(playerBoard.activePokemon.get.hasEnergies(Seq(EnergyType.Grass)))

    Given("a non base pokemon card selected by the user from his hand")
    val nonBasePokemonCard: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myEvolvedActivePokemon", 100, Nil, Nil, Nil, "myActivePokemon", Nil)
    controller.handCardSelected = Some(nonBasePokemonCard)

    When("the active pokemon location is selected by the user and contains the base pokemon for the one selected from the user hand")
    assert(playerBoard.activePokemon.nonEmpty)
    controller.selectActivePokemonLocation()

    Then("the active pokemon is evolved whit the one selected by the user hand")
    assert(playerBoard.activePokemon.nonEmpty)
    assert(playerBoard.activePokemon.get == nonBasePokemonCard)
    assert(playerBoard.activePokemon.get.energiesMap == nonBasePokemonCard.energiesMap)
    assert(playerBoard.activePokemon.get.status == nonBasePokemonCard.status)
    assert(playerBoard.activePokemon.get.actualHp == playerBoard.activePokemon.get.initialHp - (nonBasePokemonCard.initialHp - nonBasePokemonCard.actualHp))
    assert(playerBoard.discardStack.contains(basePokemonCard))
  }

  it should "manage the user game routines with bench pokemons" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdatePlayerBoard]
    }}).anyNumberOfTimes()

    val playerBoard = GameManager.playerBoard
    for (i <- playerBoard.pokemonBench.indices) {
      playerBoard.removePokemonFromBench(i)
    }
    playerBoard.pokemonBench.foreach(p => assert(p.isEmpty))

    Given("a base pokemon card selected by the user from his hand")
    val basePokemonCard: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    controller.handCardSelected = Some(basePokemonCard)

    When("a bench position is empty and selected by the user")
    val position = 0
    assert(playerBoard.pokemonBench.head.isEmpty)
    controller.selectBenchLocation(position)

    Then("the base pokemon card selected by the user should be placed in that position of the bench")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    assert(playerBoard.pokemonBench.head.get == basePokemonCard)

    Given("an energy card selected by the user from his hand")
    val energyCard = EnergyCard("1", "base1", EnergyType.Grass, EnergyCardType.basic)
    controller.handCardSelected = Some(energyCard)

    When("a bench position is not empty and selected by the user")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    controller.selectBenchLocation(position)

    Then("the energy card selected by the user should be assigned to bench pokemon in the that position")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    assert(playerBoard.pokemonBench.head.get.hasEnergies(Seq(EnergyType.Grass)))

    Given("a non base pokemon card selected by the user from his hand")
    val nonBasePokemonCard: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myEvolvedActivePokemon", 100, Nil, Nil, Nil, "myActivePokemon", Nil)
    controller.handCardSelected = Some(nonBasePokemonCard)

    When("a bench position is selected by the user and contains the base pokemon for the one selected from the user hand")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    controller.selectBenchLocation(position)

    Then("the bench pokemon is evolved whit the one selected by the user hand")
    assert(playerBoard.pokemonBench.head.nonEmpty)
    assert(playerBoard.pokemonBench.head.get == nonBasePokemonCard)
    assert(playerBoard.pokemonBench.head.get.energiesMap == nonBasePokemonCard.energiesMap)
    assert(playerBoard.pokemonBench.head.get.status == nonBasePokemonCard.status)
    assert(playerBoard.pokemonBench.head.get.actualHp == playerBoard.pokemonBench.head.get.initialHp - (nonBasePokemonCard.initialHp - nonBasePokemonCard.actualHp))
    assert(playerBoard.discardStack.contains(basePokemonCard))
  }

  def checkBoardCorrectness(board: Board): Boolean = {
    board.deck.nonEmpty &&
    board.activePokemon.isEmpty &&
    board.hand.size == GameManager.InitialHandCardNum &&
    board.prizeCards.size == GameManager.InitialPrizeCardNum &&
    board.discardStack.isEmpty &&
    !board.pokemonBench.exists(c => c.nonEmpty)
  }

  def waitForControllerThread(): Unit = {
    Thread.sleep(1000)
  }

   */

}
