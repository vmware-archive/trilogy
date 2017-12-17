package io.pivotal.trilogy.testrunner.exceptions

class SourceScriptLoadException(message: String, cause: RuntimeException) : UnrecoverableException(message, cause)