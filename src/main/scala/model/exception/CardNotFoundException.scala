package model.exception

class CardNotFoundException (private val message: String = "",
                             private val cause: Throwable = None.orNull) extends Exception(message, cause)
