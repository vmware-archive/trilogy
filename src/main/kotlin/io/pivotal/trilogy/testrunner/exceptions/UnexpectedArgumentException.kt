package io.pivotal.trilogy.testrunner.exceptions

class UnexpectedArgumentException(message: String, cause: Throwable): RuntimeException(message, cause)