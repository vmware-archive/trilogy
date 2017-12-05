package io.pivotal.trilogy.reporting

import org.jetbrains.spek.api.Spek
import kotlin.test.expect

class TestCaseResultTests : Spek({

    it("reports correctly for an empty test case") {
        val subject = TestCaseResult("Blah")
        expect(true) { subject.didPass }
        expect(0) { subject.total }
        expect(0) { subject.passed }
        expect(0) { subject.failed }
    }

    it("reports reports correctly when all the tests pass") {
        val subject = TestCaseResult("Successful", listOf(TestResult("One"), TestResult("Two")))
        expect(true) { subject.didPass }
        expect(2) { subject.total }
        expect(2) { subject.passed }
        expect(0) { subject.failed }
    }

    it("reports correctly when all the tests fail") {
        val subject = TestCaseResult("F", listOf(TestResult("F1", "Oops"), TestResult("F2", "Ouch!")))
        expect(false) { subject.didPass }
        expect(2) { subject.total }
        expect(0) { subject.passed }
        expect(2) { subject.failed }
    }

    it("reports correctly for mixed results") {
        val subject = TestCaseResult("mixed",
                listOf(TestResult("F1", "Dang"),
                        TestResult("F2", "..."),
                        TestResult("Passed")
                )
        )

        expect(false) { subject.didPass }
        expect(3) { subject.total }
        expect(2) { subject.failed }
        expect(1) { subject.passed }
    }

    it("reports a single failure when a test case fails") {
        val subject = TestCaseResult("Name", errorMessage = "Fayul")
        expect(false) { subject.didPass }
        expect(1) { subject.total }
        expect(1) { subject.failed }
    }
})