package model

import model.game.Cards.PokemonCard

object BoardTmp {
  var activePokemon: Option[PokemonCard] = null
  var defendingPokemon: Option[PokemonCard] = null
  var enemyBench: Seq[Option[PokemonCard]] = Seq()
  var myBench: Seq[Option[PokemonCard]] = Seq()

}
