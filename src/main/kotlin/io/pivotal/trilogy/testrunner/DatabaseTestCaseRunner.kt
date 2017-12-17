package io.pivotal.trilogy.testrunner

import io.pivotal.trilogy.i18n.MessageCreator.getI18nMessage
import io.pivotal.trilogy.reporting.TestCaseResult
import io.pivotal.trilogy.reporting.TestResult
import io.pivotal.trilogy.testcase.GenericTrilogyTest
import io.pivotal.trilogy.testcase.ProcedureTrilogyTest
import io.pivotal.trilogy.testcase.ProcedureTrilogyTestCase
import io.pivotal.trilogy.testcase.TestFixtures
import io.pivotal.trilogy.testcase.TrilogyAssertion
import io.pivotal.trilogy.testcase.TrilogyTest
import io.pivotal.trilogy.testcase.TrilogyTestCase
import io.pivotal.trilogy.testproject.FixtureLibrary
import io.pivotal.trilogy.testrunner.exceptions.FixtureLoadException
import io.pivotal.trilogy.testrunner.exceptions.UnexpectedArgumentException
import io.pivotal.trilogy.validators.OutputArgumentValidator
import org.springframework.dao.InvalidDataAccessApiUsageException

class DatabaseTestCaseRunner(private val testSubjectCaller: TestSubjectCaller,
                             private val assertionExecuter: AssertionExecuter,
                             private val scriptExecuter: ScriptExecuter) : TestCaseRunner {

    override fun run(trilogyTestCase: TrilogyTestCase, library: FixtureLibrary): TestCaseResult {
        val missingFixtures = trilogyTestCase.hooks.findMissingFixtures(library)
        if (missingFixtures.isNotEmpty()) {
            val errorMessage = missingFixtures.joinToString("\n") { getI18nMessage("testCaseRunner.errors.missingFixture", listOf(it)) }
            return TestCaseResult(trilogyTestCase.description, errorMessage = errorMessage)
        }

        trilogyTestCase.hooks.beforeAll.runSetupScripts(library)

        val testResults: List<TestResult>
        try {
            testResults = trilogyTestCase.runTests(library)
        } catch (e: InvalidDataAccessApiUsageException) {
            val errorMessage = if (trilogyTestCase is ProcedureTrilogyTestCase)
                getI18nMessage("testCaseRunner.errors.nonExistingStoredProcedure", listOf(trilogyTestCase.procedureName)) else e.message
            return TestCaseResult(trilogyTestCase.description, errorMessage = errorMessage)
        }

        trilogyTestCase.hooks.afterAll.runTeardownScripts(library)

        return TestCaseResult(trilogyTestCase.description, testResults)
    }

    private fun TrilogyTestCase.runTests(library: FixtureLibrary): List<TestResult> {
        return this.tests.map { test ->
            this.hooks.beforeEachTest.runSetupScripts(library)
            val testResult = test.tryProceduralTest(library, this) ?: test.tryGenericTest() ?: TestResult(test.description, "Unknown test type")
            this.hooks.afterEachTest.runTeardownScripts(library)
            testResult
        } + this.malformedTests.map { (description, errorMessage) -> TestResult(description, errorMessage) }
    }

    private fun getAssertionError(assertions: List<TrilogyAssertion>): String? {
        return assertions.map { assertion -> assertionExecuter executeReturningFailureMessage assertion }.asErrorString()
    }

    private fun GenericTrilogyTest.runTestReturningError(): String? {
        try {
            scriptExecuter.execute(this.body)
        } catch(e: RuntimeException) {
            return e.message ?: "Unknown error"
        }
        return getAssertionError(this.assertions)
    }

    private fun ProcedureTrilogyTest.runTestReturningError(testCase: ProcedureTrilogyTestCase, library: FixtureLibrary): String? {
        val outputValidator = OutputArgumentValidator(argumentTable.outputArgumentNames)

        return argumentTable.inputArgumentValues.withIndex().map { (index, inputRow) ->

            testCase.hooks.beforeEachRow.runSetupScripts(library)

            val output = try {
                testSubjectCaller.call(testCase.procedureName, argumentTable.inputArgumentNames, inputRow)
            } catch (e: InputArgumentException) {
                failureWithException(e)
            } catch (e: UnexpectedArgumentException) {
                failureWithException(e)
            }

            val currentRow = index + 1
            val rowCount = argumentTable.inputArgumentValues.count()

            val callError = output["=FAIL="].rowCallError(currentRow, rowCount)

            val outputError = if (callError == null) outputValidator.validate(argumentTable.outputArgumentValues[index], output).rowCallError(currentRow, rowCount) else null
            val assertionError = if (callError == null) getAssertionError(assertions) else null
            testCase.hooks.afterEachRow.runTeardownScripts(library)
            listOf(callError, outputError, assertionError).asErrorString()
        }.asErrorString()
    }

    private fun failureWithException(e: RuntimeException) = mapOf("=FAIL=" to e.localizedMessage)

    private fun List<String>.runSetupScripts(library: FixtureLibrary) {
        this.forEach { name ->
            try {
                scriptExecuter.execute(library.getSetupFixtureByName(name))
            } catch(e: RuntimeException) {
                val message = getI18nMessage("testCaseRunner.errors.fixtureRun",
                        listOf(name, getI18nMessage("vocabulary.fixtures.setup"), e.localizedMessage.prependIndent("    ")))
                throw FixtureLoadException(message, e)
            }
        }
    }

    private fun List<String>.runTeardownScripts(library: FixtureLibrary) {
        this.forEach { name ->
            try {
                scriptExecuter.execute(library.getTeardownFixtureByName(name))
            } catch(e: RuntimeException) {
                val message = getI18nMessage("testCaseRunner.errors.fixtureRun",
                        listOf(name, getI18nMessage("vocabulary.fixtures.teardown"), e.localizedMessage.prependIndent("    ")))
                throw FixtureLoadException(message, e)
            }
        }

    }

    private fun TrilogyTest.tryProceduralTest(library: FixtureLibrary, trilogyTestCase: TrilogyTestCase): TestResult? {
        if (this !is ProcedureTrilogyTest) return null
        val errorMessage = this.runTestReturningError(trilogyTestCase as ProcedureTrilogyTestCase, library)
        return TestResult(this.description, errorMessage)
    }

    private fun TrilogyTest.tryGenericTest(): TestResult? {
        if (this !is GenericTrilogyTest) return null
        return TestResult(this.description, this.runTestReturningError())
    }

    private fun Iterable<String?>.asErrorString(): String? {
        val nonNullList = this.filterNotNull()
        return if (nonNullList.isNotEmpty()) nonNullList.joinToString("\n") else null
    }

    private fun Any?.rowCallError(rowNumber: Int, rowCount: Int): String? {
        return if (this != null) getI18nMessage("output.errors.forRow", listOf(rowNumber, rowCount, this)) else null
    }

    private fun TestFixtures.findMissingFixtures(library: FixtureLibrary): List<String> {
        return this.findMissingSetupFixtures(library) + this.findMissingTeardownFixtures(library)
    }

    private fun TestFixtures.findMissingSetupFixtures(library: FixtureLibrary): List<String> {
        return (this.beforeAll + this.beforeEachTest + this.beforeEachRow).map {
            try {
                library.getSetupFixtureByName(it)
                null
            } catch(e: NullPointerException) {
                it
            }
        }.filterNotNull()
    }

    private fun TestFixtures.findMissingTeardownFixtures(library: FixtureLibrary): List<String> {
        return (this.afterAll + this.afterEachTest + this.afterEachRow).map {
            try {
                library.getTeardownFixtureByName(it)
                null
            } catch(e: NullPointerException) {
                it
            }
        }.filterNotNull()
    }
}

