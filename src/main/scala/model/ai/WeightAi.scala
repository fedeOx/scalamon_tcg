package model.ai

object WeightAi extends Enumeration {
  val PokemonDmg = 10
  val WeakPokemon: Int = -80
  val HasEnergy = 15
  val IsEvolution = 10
  val WithdrawCost = 20
  val EvolutionInHand = 20
  val KO: Int = -300
}

object WeightCalculatorType extends Enumeration {
  val PlaceCard , WithDraw = Value
}