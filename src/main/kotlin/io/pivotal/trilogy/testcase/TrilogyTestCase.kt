package io.pivotal.trilogy.testcase

interface TrilogyTestCase {
    val description: String
    val tests: List<TrilogyTest>
    val hooks: TestCaseFixtures
    val malformedTests: List<MalformedTrilogyTest>
}