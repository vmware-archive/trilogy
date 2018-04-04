package io.pivotal.trilogy.live.oracle

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

    fun bootTrilogyController(): TrilogyController {
        val controller = TrilogyController()
        val dataSource = DatabaseHelper.oracleDataSource()
        val jdbcTemplate = JdbcTemplate(dataSource)
        val testSubjectCaller = DatabaseTestSubjectCaller(dataSource)
        val scriptExecuter = DatabaseScriptExecuter(jdbcTemplate)
        val assertionExecuter = DatabaseAssertionExecuter(scriptExecuter)
        val testCaseRunner = DatabaseTestCaseRunner(testSubjectCaller, assertionExecuter, scriptExecuter)
        controller.testProjectRunner = DatabaseTestProjectRunner(testCaseRunner, scriptExecuter)
        return controller
    }

    describe("execution") {
        val controller = bootTrilogyController()

        describe("simple cases") {
            it("succeeds for a simple case") {
                val options = TrilogyApplicationOptions("src/test/resources/testcases/should_pass.stt", shouldDisplayHelp = false)
                expect(true) { controller.run(options).testCaseResults.all { it.didPass } }
            }

            it("fails for a simple case") {
                val options = TrilogyApplicationOptions("src/test/resources/testcases/should_fail.stt", shouldDisplayHelp = false)
                expect(false) { controller.run(options).testCaseResults.all { it.didPass } }
            }
        }

        describe("tests with assertions") {
            it("succeed when the assertions pass") {
                val options = TrilogyApplicationOptions("src/test/resources/testcases/should_pass_with_sql.stt", shouldDisplayHelp = false)
                expect(true) { controller.run(options).testCaseResults.all { it.didPass } }
            }

            it("fails when the assertions raise an error") {
                val options = TrilogyApplicationOptions("src/test/resources/testcases/should_fail_with_sql.stt", shouldDisplayHelp = false)
                expect(false) { controller.run(options).testCaseResults.all { it.didPass } }
            }
        }

        describe("multiple tests in a test case") {
            it("succeeds when all the tests succeed") {
                val options = TrilogyApplicationOptions("src/test/resources/testcases/multiple/shouldPass.stt", shouldDisplayHelp = false)
                expect(true) { controller.run(options).testCaseResults.all { it.didPass } }
            }

            it("fails when one of the tests is failing") {
                val options = TrilogyApplicationOptions("src/test/resources/testcases/multiple/shouldFail.stt", shouldDisplayHelp = false)
                expect(false) { controller.run(options).testCaseResults.all { it.didPass } }
            }
        }

        describe("projects") {
            beforeEach { DatabaseHelper.executeScript("simpleProjectCleanup") }

            it("passes for a simple project") {
                val options = TrilogyApplicationOptions(testProjectPath = "src/test/resources/projects/simple", shouldDisplayHelp = false)
                val testCaseResult = controller.run(options).testCaseResults
                expect(true) { testCaseResult.all { it.didPass } }
                expect(2) { testCaseResult.fold(0) { acc, result -> acc + result.passed } }
                expect(0) { testCaseResult.fold(0) { acc, result -> acc + result.failed } }
            }

            it("passes for a simple schema") {
                DatabaseHelper.executeScript("dropClients")
                val options = TrilogyApplicationOptions(testProjectPath = "src/test/resources/projects/schema", shouldDisplayHelp = false)
                val testCaseResult = controller.run(options).testCaseResults
                expect(true) { testCaseResult.all { it.didPass } }
                expect(1) { testCaseResult.fold(0) { acc, result -> acc + result.passed } }
                expect(0) { testCaseResult.fold(0) { acc, result -> acc + result.failed } }
            }

            describe("fixtures") {
                val options = TrilogyApplicationOptions(testProjectPath = "src/test/resources/projects/setup_teardown", shouldDisplayHelp = false)
                it("runs the test project with fixtures") {
                    val testCaseResult = controller.run(options).testCaseResults
                    expect(true) { testCaseResult.all { it.didPass } }
                    expect(0) { testCaseResult.fold(0) { acc, result -> acc + result.failed } }
                    expect(2) { testCaseResult.fold(0) { acc, result -> acc + result.passed } }
                }
            }

            describe("errors") {
                val options = TrilogyApplicationOptions(testProjectPath = "src/test/resources/projects/errors", shouldDisplayHelp = false)
                it("validates the errors") {
                    val result = controller.run(options)
                    expect(false) { result.unrecoverableFailure }

                    val testCaseResult = result.testCaseResults
                    expect(false) { testCaseResult.all { it.didPass } }
                    expect(1) { testCaseResult.fold(0) { acc, test -> acc + test.passed } }
                    expect(3) { testCaseResult.fold(0) { acc, test -> acc + test.failed } }
                }

            }

            it("fails with broken source") {
                val options = TrilogyApplicationOptions(testProjectPath = "src/test/resources/projects/broken_source", shouldDisplayHelp = false)
                val result = controller.run(options)
                expect(true) { result.hasFatalFailure }
                expect(true) { result.unrecoverableFailure }
            }


        }

    }

})
