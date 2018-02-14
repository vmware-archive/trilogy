package io.pivotal.trilogy.testcase

data class ProcedureTrilogyTest(
        override val description: String,
        val argumentTable: TestArgumentTable,
        override val assertions: List<TrilogyAssertion>,
        override val hooks: TestFixtures
) : TrilogyTest