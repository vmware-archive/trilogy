package io.pivotal.trilogy.reporting

data class TestCaseResult(val testCaseName: String, val tests: List<TestResult> = emptyList(), val errorMessage: String? = null) {
    val didPass: Boolean get() = failedTests.isEmpty() and !hasTestCaseFailed
    val total: Int = if (hasTestCaseFailed) 1 else tests.count()
    val failedTests: List<TestResult> by lazy { tests.filter { it.hasFailed } }
    private val passedTests: List<TestResult> by lazy { tests.filter { it.hasSucceeded } }
    private val hasTestCaseFailed: Boolean get() = !errorMessage.isNullOrBlank()
    val passed: Int get() = passedTests.count()
    val failed: Int get() = if (hasTestCaseFailed) 1 else failedTests.count()
}