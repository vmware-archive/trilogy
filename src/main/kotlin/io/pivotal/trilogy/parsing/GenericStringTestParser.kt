package io.pivotal.trilogy.parsing

import io.pivotal.trilogy.i18n.MessageCreator.getI18nMessage
import io.pivotal.trilogy.parsing.exceptions.test.MissingBody
import io.pivotal.trilogy.parsing.exceptions.test.MissingDescription
import io.pivotal.trilogy.testcase.GenericTrilogyTest
import io.pivotal.trilogy.testcase.TestFixtures

class GenericStringTestParser(testBody: String) : BaseStringTestParser(testBody) {

    private val headerlessBody: String by lazy { testBody.replace(testHeaderRegex, "") }

    private val testSection: String by lazy { testBody.split("### ASSERTIONS", limit = 2).first()}

    private val test: String? by lazy {
        Regex("```(.+?)```", RegexOption.DOT_MATCHES_ALL).find(testSection)?.groupValues.orEmpty().getOrNull(1)
    }

    override val description: String? by lazy {
        Regex("(###|```).*", RegexOption.DOT_MATCHES_ALL).replace(headerlessBody, "").trim()
    }

    init {
        validate()
    }

    override fun getTest(): GenericTrilogyTest {
        return GenericTrilogyTest(
                description!!.trim(),
                test!!.trim(), parseAssertions(),
                TestFixtures(parseBeforeHooks(), parseAfterHooks()))
    }

    override fun validate() {
        super.validate()
        if (description == null || description!!.isEmpty())
            throw MissingDescription(
                    getI18nMessage("testParser.generic.errors.missingDescription.message"),
                    getI18nMessage("testParser.generic.errors.missingDescription.testName")
            )
        if (test == null) throw MissingBody(getI18nMessage("testParser.generic.errors.missingBody.message"), description!!)
    }

}