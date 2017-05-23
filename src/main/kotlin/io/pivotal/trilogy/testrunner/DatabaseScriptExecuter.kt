package io.pivotal.trilogy.testrunner

import io.pivotal.trilogy.i18n.MessageCreator
import org.flywaydb.core.internal.dbsupport.DbSupportFactory
import org.flywaydb.core.internal.dbsupport.SqlScript
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.SQLException

class DatabaseScriptExecuter(val jdbcTemplate: JdbcTemplate) : ScriptExecuter {

    override fun execute(scriptBody: String) {
        val dbSupport = try {
            DbSupportFactory.createDbSupport(jdbcTemplate.dataSource.connection, false)
        } catch (e : SQLException) {
            throw UnrecoverableException(MessageCreator.getI18nMessage("connectionFailure", listOf(e.localizedMessage)), e)
        }

        val sqlScript = SqlScript(scriptBody, dbSupport)

        sqlScript.sqlStatements.forEach { statement ->
            jdbcTemplate.execute(statement.sql)
        }
    }

}