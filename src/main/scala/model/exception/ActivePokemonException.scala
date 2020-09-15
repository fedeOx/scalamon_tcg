package model.exception

class ActivePokemonException(private val message: String = "",
                             private val cause: Throwable = None.orNull) extends Exception(message, cause)