package at.itdo.tcrunner.run

import at.itdo.tcrunner.Settings
import at.itdo.tcrunner.run.parsing.ConsoleProperties
import at.itdo.tcrunner.run.parsing.TestCafeProcessWrapper
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
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
        val workingDir = when {
            configuration.getWorkingDirectory().isNotBlank() -> File(configuration.getWorkingDirectory())
            settings.workingDirectory.isNotBlank() -> File(settings.workingDirectory)
            else -> findNodeProjectRoot(File(configuration.getScriptPath())) ?: File(project.basePath ?: ".")
        }

        // Build command based on configuration
        val command = buildTestCafeCommand(settings)

        val commandLine = GeneralCommandLine()
            .withWorkDirectory(workingDir)
            .withEnvironment(EnvironmentUtil.getEnvironmentMap())

        return when {
            SystemInfo.isWindows -> {
                commandLine
                    .withExePath("powershell")
                    .withParameters("-Command", "Write-Host \"Working directory: \$(Get-Location)\"; $command")
            }
            else -> {
                // Linux/macOS: Use bash as login shell to load full environment
                commandLine
                    .withExePath("bash")
                    .withParameters("-l", "-c", "echo \"Working directory: \$(pwd)\" && $command")
            }
        }
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

    private fun findNodeProjectRoot(startFile: File): File? {
        var currentDir = if (startFile.isDirectory) startFile else startFile.parentFile
        try {
            while (currentDir != null) {
                // Check for package.json or node_modules
                if (File(currentDir, "package.json").exists() || File(currentDir, "node_modules").exists()) {
                    return currentDir
                }
                currentDir = currentDir.parentFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
