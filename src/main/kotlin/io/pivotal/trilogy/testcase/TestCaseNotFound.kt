package io.pivotal.trilogy.testcase

import io.pivotal.trilogy.testrunner.UnrecoverableException

class TestCaseNotFound(message: String, cause: Throwable = RuntimeException()): UnrecoverableException(message, cause)