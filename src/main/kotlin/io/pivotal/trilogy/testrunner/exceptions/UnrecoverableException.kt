package io.pivotal.trilogy.testrunner.exceptions

open class UnrecoverableException(message: String, cause: Throwable) : RuntimeException(message, cause)