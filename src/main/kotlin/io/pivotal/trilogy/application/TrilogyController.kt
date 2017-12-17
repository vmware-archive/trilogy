package io.pivotal.trilogy.application

import io.pivotal.trilogy.i18n.MessageCreator.getI18nMessage
import io.pivotal.trilogy.testproject.TestBuildException
import io.pivotal.trilogy.testproject.TestProjectBuilder
import io.pivotal.trilogy.testproject.TestProjectResult
import io.pivotal.trilogy.testrunner.TestProjectRunner
import io.pivotal.trilogy.testrunner.exceptions.MalformedDatabaseURLException
import io.pivotal.trilogy.testrunner.exceptions.UnrecoverableException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller

@Controller
open class TrilogyController {

    @Autowired
    lateinit var testProjectRunner: TestProjectRunner

    @Value("\${spring.datasource.url:undefined}")
    lateinit var jdbcUrl: String

    fun run(options: TrilogyApplicationOptions): TestProjectResult {
        val testProject = try {
            TestProjectBuilder.build(options)
        } catch (e: TestBuildException) {
            return TestProjectResult(emptyList(), failureMessage = e.localizedMessage)
        }

        return try {
            testProjectRunner.run(testProject)
        } catch(e: UnrecoverableException) {
            TestProjectResult(emptyList(), failureMessage = e.localizedMessage, unrecoverableFailure = true)
        } catch (e: MalformedDatabaseURLException) {
            val message = getI18nMessage("databaseURLError", listOf(jdbcUrl))
            TestProjectResult(emptyList(), failureMessage = "$message\n${e.localizedMessage}")
        }

    }
}

