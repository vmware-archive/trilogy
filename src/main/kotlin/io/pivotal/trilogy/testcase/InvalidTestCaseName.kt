package io.pivotal.trilogy.testcase

import io.pivotal.trilogy.testrunner.UnrecoverableException

class InvalidTestCaseName(message: String, cause: Throwable = RuntimeException()): UnrecoverableException(message, cause)