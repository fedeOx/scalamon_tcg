package model.exception

class MissingEnergyException(private val message: String = "",
                             private val cause: Throwable = None.orNull) extends Exception(message, cause)
