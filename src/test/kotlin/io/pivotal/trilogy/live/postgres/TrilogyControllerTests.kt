package io.pivotal.trilogy.live.postgres

import io.pivotal.trilogy.application.TrilogyApplicationOptions
import io.pivotal.trilogy.application.TrilogyController
import io.pivotal.trilogy.test_helpers.DatabaseHelper
import io.pivotal.trilogy.testrunner.DatabaseAssertionExecuter
import io.pivotal.trilogy.testrunner.DatabaseScriptExecuter
import io.pivotal.trilogy.testrunner.DatabaseTestCaseRunner
import io.pivotal.trilogy.testrunner.DatabaseTestProjectRunner
import io.pivotal.trilogy.testrunner.DatabaseTestSubjectCaller
import org.jetbrains.spek.api.Spek
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.expect

class TrilogyControllerTests : Spek({

    fun bootTrilogyController(user: String): TrilogyController {
        val controller = TrilogyController()
        val dataSource = DatabaseHelper.postgresDatasource(user)
        val jdbcTemplate = JdbcTemplate(dataSource)
        val testSubjectCaller = DatabaseTestSubjectCaller(dataSource)
        val scriptExecuter = DatabaseScriptExecuter(jdbcTemplate)
        val assertionExecuter = DatabaseAssertionExecuter(scriptExecuter)
        val testCaseRunner = DatabaseTestCaseRunner(testSubjectCaller, assertionExecuter, scriptExecuter)
        controller.testProjectRunner = DatabaseTestProjectRunner(testCaseRunner, scriptExecuter)
        return controller
    }

    it("errors correctly when an incorrect user is given") {
        val controller = bootTrilogyController("steve")
        val options = TrilogyApplicationOptions(testProjectPath = "src/test/resources/projects/pg_generic")

        val testProjectResult = controller.run(options)

        expect(true) { testProjectResult.fatalFailure }
    }

    it("succeeds when the connection options are okay") {
        val controller = bootTrilogyController("app_user")
        val options = TrilogyApplicationOptions(testProjectPath = "src/test/resources/projects/pg_generic")

        val testProjectResult = controller.run(options)

        expect(false) { testProjectResult.fatalFailure }
    }
})