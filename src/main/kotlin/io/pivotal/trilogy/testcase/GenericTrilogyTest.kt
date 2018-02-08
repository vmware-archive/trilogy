package io.pivotal.trilogy.testcase

data class GenericTrilogyTest(
        override val description: String, val body: String,
        override val assertions: List<TrilogyAssertion>,
        override val testFixtures: TestFixtures
) : TrilogyTest