package at.itdo.tcrunner.run

import at.itdo.tcrunner.TestCafeSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import java.io.File

class TestCafeRunProfileState(
    environment: ExecutionEnvironment,
    private val configuration: TestCafeRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val commandLine = createCommandLine()
        return ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
    }

    @Throws(ExecutionException::class)
    private fun createCommandLine(): GeneralCommandLine {
        val project = configuration.project
        val settings = TestCafeSettings.getInstance(project)

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

    private fun buildTestCafeCommand(settings: TestCafeSettings): String {
        // Get custom command or determine appropriate default template
        val command = configuration.getCustomCommand().ifBlank {
            // Determine command type and get appropriate template from settings
            when {
                configuration.getTestFilter()
                    .isNotBlank() -> settings.getCommandTemplate(TestCafeSettings.CommandType.TEST)

                configuration.getFixtureFilter()
                    .isNotBlank() -> settings.getCommandTemplate(TestCafeSettings.CommandType.FIXTURE)

                else -> settings.getCommandTemplate(TestCafeSettings.CommandType.FILE)
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
