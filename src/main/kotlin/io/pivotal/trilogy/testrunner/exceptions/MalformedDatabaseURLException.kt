package io.pivotal.trilogy.testrunner.exceptions

import java.sql.SQLException

class MalformedDatabaseURLException(message: String, cause: SQLException) : RuntimeException(message, cause)