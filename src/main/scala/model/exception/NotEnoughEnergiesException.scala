package model.exception

class NotEnoughEnergiesException(private val message: String = "",
                                  private val cause: Throwable = None.orNull) extends Exception(message, cause)
