package io.pivotal.trilogy.parsing

import io.pivotal.trilogy.application.TrilogyApplicationOptions
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

object TrilogyApplicationOptionsParser {

    fun parse(arguments: Array<String>): TrilogyApplicationOptions {
        val options = Options().apply {
            addOption("", "project", true, "Path to the test project directory")
            addOption("", "skip_schema_load", false, "Use this flag to skip loading schema from the project")
            addOption("h", "help", false, "Display application usage")
        }
        val command = try {
            DefaultParser().parse(options, arguments.filter { it.isValidOption }.toTypedArray())
        } catch (e: ParseException) {
            println(e.message)
            throw e
        }

        return trilogyApplicationOptions(command)
    }

    private fun trilogyApplicationOptions(command: CommandLine): TrilogyApplicationOptions {
        return if (command.args.any())
            testCaseApplicationOptions(command)
        else
            testProjectApplicationOptions(command)
    }

    private fun testProjectApplicationOptions(command: CommandLine): TrilogyApplicationOptions {
        val testProjectPath = command.getOptionValue("project") ?: ""
        val shouldSkipSchema = command.hasOption("skip_schema_load")
        val shouldDisplayHelp = command.hasOption("help")
        return TrilogyApplicationOptions(testProjectPath = testProjectPath, shouldSkipSchema = shouldSkipSchema, shouldDisplayHelp = shouldDisplayHelp)
    }

    private fun testCaseApplicationOptions(command: CommandLine): TrilogyApplicationOptions {
        val shouldDisplayHelp = command.hasOption("help")
        return TrilogyApplicationOptions(testCaseFilePath = command.args.first(), shouldDisplayHelp = shouldDisplayHelp)
    }

    private val String.isValidOption: Boolean get() {
        val validOptions = listOf("--project=", "--skip_schema_load", "--help")
        return this.startsWith("--").not() || validOptions.any { this.startsWith(it) }
    }

}