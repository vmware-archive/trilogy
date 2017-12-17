package io.pivotal.trilogy.mocks

import io.pivotal.trilogy.reporting.TestCaseResult
import io.pivotal.trilogy.testcase.TrilogyTestCase
import io.pivotal.trilogy.testproject.FixtureLibrary
import io.pivotal.trilogy.testrunner.TestCaseRunner

class TestCaseRunnerMock : TestCaseRunner {
    var runCount = 0
    var runResult = TestCaseResult("Mock test case")
    var runArgument: TrilogyTestCase? = null
    var shouldFailExecution = false
    var failureException: Exception = RuntimeException("SQL Script exception")


    override fun run(trilogyTestCase: TrilogyTestCase, library: FixtureLibrary): TestCaseResult {
        if (shouldFailExecution) {
            throw failureException
        }
        runCount++
        runArgument = trilogyTestCase
        return runResult
    }
}