package model.pokemonEffect

import model.game.Cards.PokemonCard

object BoardTmp {
  var activePokemon: PokemonCard = null
  var defendingPokemon: PokemonCard = null
  var enemyBench: Seq[PokemonCard] = Seq()
  var myBench: Seq[PokemonCard] = Seq()

}