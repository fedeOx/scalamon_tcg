package model.ai

import model.card.{EnergyCard, PokemonCard}
import model.game.Board


object WeightCalculator {

  /**
   * returns the list in decreasing order of the weights of the pokemon on the bench
   *
   * @param opponentBoard board of the opponent
   * @param playerBoard   board of the player
   * @param weightType    pokemon weight type
   * @return
   */
  def calculateOrderedSeq(seqToOrder: Seq[PokemonCard], opponentBoard: Board, playerBoard: Board, weightType: WeightCalculatorType.Value): Seq[PokemonWithWeightImpl] = {
    def createWeightedCard(pokemon: PokemonCard, calculateweight: (PokemonCard, Board, Board) => Int) = Seq(PokemonWithWeightImpl(pokemon, calculateweight(pokemon, opponentBoard, playerBoard)))
    var orderedSeq: Seq[PokemonWithWeightImpl] = Seq.empty
    weightType.toString match {
      case "WithDraw" => orderedSeq = seqToOrder.flatMap(pkm => createWeightedCard(pkm, calculateWeightForWithdraw))
      case "PlaceCard" => orderedSeq = seqToOrder.flatMap(pkm => createWeightedCard(pkm, calculateWeightForPlaceCard))
    }
    orderedSeq.sortWith(_.weight > _.weight)
  }

  /**
   * calculates the weight of the pokemon passed based on the type of weightType given to it
   *
   * @param pokemon       pokemon to calculate the weight of
   * @param opponentBoard board of the opponent
   * @param playerBoard   board of the player
   * @param weightType    pokemon weight type
   * @return the weight of the pokemon
   */
  def calculatePokemonWeight(pokemon: PokemonCard, opponentBoard: Board, playerBoard: Board, weightType: WeightCalculatorType.Value): Int = {
    weightType.toString match {
      case  "WithDraw" => calculateWeightForWithdraw(pokemon, opponentBoard, playerBoard)
      case  "PlaceCard" => calculateWeightForPlaceCard(pokemon, opponentBoard, playerBoard)
    }
  }

  /**
   * Calculate the weights of the cards for the withdrawal of the active pokemon
   *
   * @param pokemon       pokemon to calculate the weight of
   * @param playerBoard   board of the player
   * @param opponentBoard board of the opponent
   * @return pokemon weight
   */
  private def calculateWeightForWithdraw(pokemon:PokemonCard, playerBoard: Board, opponentBoard: Board): Int = {
    var totalweight = 0

    totalweight -= (pokemon.initialHp - pokemon.actualHp)
    if (pokemon.weaknesses.nonEmpty) {
      if (pokemon.weaknesses.head.energyType == playerBoard.activePokemon.get.pokemonTypes.head)
        totalweight += WeightAi.WeakPokemon
    }
    totalweight += pokemon.totalEnergiesStored * WeightAi.HasEnergy
    if (pokemon.evolutionName != "")
      totalweight += WeightAi.IsEvolution

    if (pokemon == opponentBoard.activePokemon.get)
      totalweight += pokemon.retreatCost.size * WeightAi.WithdrawCost

    if (pokemon.isKO)
      totalweight += WeightAi.KO

    totalweight
  }

  /**
   * Calculate the weight of the pokemon for its placement on the playing field
   *
   * @param pokemon pokemon to calculate the weight of
   * @return pokemon weight
   */
  private def calculateWeightForPlaceCard(pokemon: PokemonCard, opponentBoard: Board, playerBoard: Board): Int = {
    var totalweight = 0
    val getPokemonCard = opponentBoard.hand.filter(pkm => pkm.isInstanceOf[PokemonCard])
    val getEnergy = opponentBoard.hand.filter(energy => energy.isInstanceOf[EnergyCard])
    if (getPokemonCard.exists(pkm => pkm.asInstanceOf[PokemonCard].evolutionName == pokemon.asInstanceOf[PokemonCard].name))
      totalweight += WeightAi.EvolutionInHand
    totalweight += getEnergy.count(card => card.asInstanceOf[EnergyCard].energyType == pokemon.pokemonTypes.head) * WeightAi.HasEnergy

    totalweight
  }
}


/**
 * creates a pokemon with associated weight value
 */
sealed trait PokemonWithWeight {
  def pokemonCard: PokemonCard
  def weightValue: Int
}

case class PokemonWithWeightImpl(_pokemonCard: PokemonCard, weight: Int) extends PokemonWithWeight {
  override def pokemonCard: PokemonCard = _pokemonCard
  override def weightValue: Int = weight
}

