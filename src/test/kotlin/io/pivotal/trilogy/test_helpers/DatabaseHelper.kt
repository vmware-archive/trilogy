package io.pivotal.trilogy.test_helpers

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

object DatabaseHelper {

    val oracleJdbcUrl = System.getenv("DB_URL") ?: "jdbc:oracle:thin:@192.168.99.100:32769:xe"
    val postgresJdbcUrl = "jdbc:postgresql://localhost:5432/app_user"

    fun oracleDataSource(): DataSource {
        return DriverManagerDataSource().apply {
            setDriverClassName("oracle.jdbc.driver.OracleDriver")
            url = oracleJdbcUrl
            username = "app_user"
            password = "secret"
        }
    }

    fun postgresDatasource(user: String): DataSource {
        return DriverManagerDataSource().apply {
            setDriverClassName("org.postgresql.Driver")
            url = postgresJdbcUrl
            username = user
            password = "secret"
        }
    }

    fun jdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(oracleDataSource())
    }


    fun executeScript(scriptName: String) {
        jdbcTemplate().execute(ResourceHelper.getScriptByName(scriptName))
    }

}
