package io.pivotal.trilogy.testrunner

import io.pivotal.trilogy.i18n.MessageCreator.getI18nMessage
import io.pivotal.trilogy.testrunner.exceptions.MalformedDatabaseURLException
import io.pivotal.trilogy.testrunner.exceptions.UnrecoverableException
import org.flywaydb.core.internal.dbsupport.DbSupportFactory
import org.flywaydb.core.internal.dbsupport.SqlScript
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.SQLException

class DatabaseScriptExecuter(val jdbcTemplate: JdbcTemplate) : ScriptExecuter {

    override fun execute(scriptBody: String) {
        val dbSupport = try {
            DbSupportFactory.createDbSupport(jdbcTemplate.dataSource.connection, false)
        } catch (e: SQLException) {
            if (e.cause is ClassNotFoundException) {
                throw MalformedDatabaseURLException(e.localizedMessage, e)
            } else {
                throw UnrecoverableException(getI18nMessage("connectionFailure", listOf(e.localizedMessage)), e)
            }
        }

        val sqlScript = SqlScript(scriptBody, dbSupport)

        sqlScript.sqlStatements.forEach { statement ->
            jdbcTemplate.execute(statement.sql)
        }
    }

}