package io.pivotal.trilogy.testproject

import io.pivotal.trilogy.testrunner.UnrecoverableException

class TestProjectNotFound(message: String, cause: Throwable = RuntimeException()): UnrecoverableException(message, cause)