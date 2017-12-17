package io.pivotal.trilogy.testrunner.exceptions

class SchemaLoadFailedException(message: String, exception: RuntimeException) : UnrecoverableException(message, exception)