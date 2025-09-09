package at.itdo.testcafe.run

import at.itdo.testcafe.TestCafeSettings
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
        // If custom command is specified, use it directly
        val customCommand = configuration.getCustomCommand()
        if (customCommand.isNotBlank()) {
            return replaceVariables(customCommand)
        }
        
        // Otherwise build command from individual options
        val parts = mutableListOf<String>()
        parts.add("npx testcafe")
        
        // Browser
        var browser = configuration.getBrowser()
        if (configuration.getHeadlessMode() && !browser.contains(":headless")) {
            browser += ":headless"
        }
        parts.add(browser)
        
        // Script path
        val scriptPath = configuration.getScriptPath()
        if (scriptPath.isNotBlank()) {
            parts.add(scriptPath)
        }
        
        // Test filter
        val testFilter = configuration.getTestFilter()
        if (testFilter.isNotBlank()) {
            parts.add("-t")
            parts.add("\"$testFilter\"")
        }
        
        // Fixture filter
        val fixtureFilter = configuration.getFixtureFilter()
        if (fixtureFilter.isNotBlank()) {
            parts.add("-f")
            parts.add("\"$fixtureFilter\"")
        }
        
        // Live mode
        if (configuration.getLiveMode()) {
            parts.add("--live")
        }
        
        return parts.joinToString(" ")
    }
    
    private fun replaceVariables(command: String): String {
        return command
            .replace("{filePath}", configuration.getScriptPath())
            .replace("{testName}", configuration.getTestFilter())
            .replace("{fixtureName}", configuration.getFixtureFilter())
            .replace("{browser}", configuration.getBrowser())
            .replace("{workingDirectory}", configuration.getWorkingDirectory())
    }
}