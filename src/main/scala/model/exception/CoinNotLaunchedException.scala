package model.exception

class CoinNotLaunchedException (private val message: String = "",
                                private val cause: Throwable = None.orNull) extends Exception(message, cause)
