package model.exception

class BenchPokemonException(private val message: String = "",
                            private val cause: Throwable = None.orNull) extends Exception(message, cause)
