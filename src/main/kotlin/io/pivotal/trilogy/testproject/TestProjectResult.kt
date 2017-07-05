package io.pivotal.trilogy.testproject

import io.pivotal.trilogy.reporting.TestCaseResult
import io.pivotal.trilogy.testcase.MalformedTrilogyTestCase

data class TestProjectResult(
        val testCaseResults: List<TestCaseResult>,
        val malformedTestCases: List<MalformedTrilogyTestCase> = emptyList(),
        val failureMessage: String? = null,
        val unrecoverableFailure: Boolean = false) {
    val hasFatalFailure: Boolean get() = ! failureMessage.isNullOrBlank()
    val hasTestFailures: Boolean get() = testCaseResults.any { it.failed > 0 }
}