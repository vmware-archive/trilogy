package io.pivotal.trilogy.testcase

class GenericTrilogyTestCase (override val description: String,
                              override val tests: List<GenericTrilogyTest>,
                              override val hooks: TestFixtures,
                              override val malformedTests: List<MalformedTrilogyTest> = emptyList()) : TrilogyTestCase