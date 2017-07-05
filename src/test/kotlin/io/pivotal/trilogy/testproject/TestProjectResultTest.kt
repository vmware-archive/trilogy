package io.pivotal.trilogy.testproject

import io.pivotal.trilogy.reporting.TestCaseResult
import io.pivotal.trilogy.reporting.TestResult
import io.pivotal.trilogy.testcase.MalformedTrilogyTestCase
import org.jetbrains.spek.api.Spek
import kotlin.test.expect

class TestProjectResultTest : Spek({
    it("is not fatally failed by default") {
        expect(false) { TestProjectResult(emptyList()).hasFatalFailure }
    }

    it("has fatally failed when a failure message is present") {
        expect(true) { TestProjectResult(emptyList(), failureMessage = "message").hasFatalFailure }
    }

    it("is not failed by default") {
        expect(false) { TestProjectResult(emptyList()).hasTestFailures }
    }

    it("has failed when at least one test is failed") {
        expect(true) { TestProjectResult(listOf(TestCaseResult("", listOf(TestResult("", "ERROR"))))).hasTestFailures }
    }

    it("has failed when there is a failing test") {
        expect(true) { TestProjectResult(listOf(TestCaseResult("", listOf(TestResult("", "ERROR"))))).hasFailed }
    }

    it("has failed when there is a test case runtime error") {
        expect(true) { TestProjectResult(listOf(TestCaseResult("", errorMessage = "Foo"))).hasFailed }
    }

    it("has failed when there is a project runtime error") {
        expect(true) { TestProjectResult(emptyList(), failureMessage = "message").hasFailed }
    }

    it("has failed when there is a malformed test case") {
        expect(true) { TestProjectResult(emptyList(), malformedTestCases = listOf(MalformedTrilogyTestCase("name", "bad thing"))).hasFailed }
    }


})