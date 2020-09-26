package model.exception

class InvalidOperationException(private val message: String = "",
                                private val cause: Throwable = None.orNull) extends Exception(message, cause)
