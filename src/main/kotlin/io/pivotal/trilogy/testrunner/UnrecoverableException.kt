package io.pivotal.trilogy.testrunner

open class UnrecoverableException(message: String, cause: Throwable) : RuntimeException(message, cause)