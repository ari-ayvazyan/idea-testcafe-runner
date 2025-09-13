package at.itdo.tcrunner.run

import at.itdo.tcrunner.Settings
import at.itdo.tcrunner.run.parsing.TestCafeProcessWrapper
import at.itdo.tcrunner.run.parsing.ConsoleProperties
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import java.io.File

class RunProfileState(
    environment: ExecutionEnvironment,
    private val configuration: RunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val commandLine = createCommandLine()

        // Create process and wrap it with TestCafe service message handler
        val process = commandLine.createProcess()
        return TestCafeProcessWrapper.createProcessHandler(
            process,
            commandLine.commandLineString,
            configuration.getScriptPath()
        )
    }

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        val processHandler = startProcess()

        // Create SM Test Runner console with proper properties
        val properties = ConsoleProperties(configuration, executor)
        val consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(
            "TestCafe",
            processHandler,
            properties
        )

        return com.intellij.execution.DefaultExecutionResult(consoleView, processHandler)
    }

    @Throws(ExecutionException::class)
    private fun createCommandLine(): GeneralCommandLine {
        val project = configuration.project
        val settings = Settings.getInstance(project)

        // Determine working directory
        val workingDir = if (configuration.getWorkingDirectory().isNotBlank()) {
            File(configuration.getWorkingDirectory())
        } else {
            File(project.basePath ?: ".")
        }

        // Build command based on configuration
        val command = buildTestCafeCommand(settings)

        return GeneralCommandLine()
            .withWorkDirectory(workingDir)
            .withExePath("bash")
            .withParameters("-c", command)
    }

    private fun buildTestCafeCommand(settings: Settings): String {
        // Get custom command or determine appropriate default template
        val command = configuration.getCustomCommand().ifBlank {
            // Determine command type and get appropriate template from settings
            when {
                configuration.getTestFilter()
                    .isNotBlank() -> settings.getCommandTemplate(Settings.CommandType.TEST)

                configuration.getFixtureFilter()
                    .isNotBlank() -> settings.getCommandTemplate(Settings.CommandType.FIXTURE)

                else -> settings.getCommandTemplate(Settings.CommandType.FILE)
            }
        }

        // Always use replaceVariables on the command
        return replaceVariables(command)
    }

    private fun replaceVariables(command: String): String {
        // Build browser string with headless mode if enabled
        var browser = configuration.getBrowser()
        if (configuration.getHeadlessMode() && !browser.contains(":headless")) {
            browser += ":headless"
        }

        return command
            .replace("{filePath}", configuration.getScriptPath())
            .replace("{testName}", configuration.getTestFilter())
            .replace("{fixtureName}", configuration.getFixtureFilter())
            .replace("{browser}", browser)
            .replace("{workingDirectory}", configuration.getWorkingDirectory())
    }
}
