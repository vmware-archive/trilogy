package io.pivotal.trilogy.testrunner.exceptions

class FixtureLoadException(message: String, cause: RuntimeException) : UnrecoverableException(message, cause)