package at.itdo.tcrunner.run

import com.intellij.execution.process.*
import com.intellij.openapi.util.Key

/**
 * Factory for creating TestCafe process handlers with SM Test Runner integration
 */
object TestCafeProcessWrapper {
    
    fun createProcessHandler(
        process: Process,
        commandLine: String,
        testFilePath: String
    ): ProcessHandler {
        val processHandler = KillableColoredProcessHandler(process, commandLine)
        
        // Add listener that converts TestCafe output to TeamCity service messages
        processHandler.addProcessListener(TestCafeOutputListener(testFilePath))
        
        return processHandler
    }
}

/**
 * Process listener that converts TestCafe output to TeamCity service messages
 */
private class TestCafeOutputListener(
    private val testFilePath: String
) : ProcessAdapter() {

    private val outputBuffer = StringBuilder()
    private var currentFixture: String? = null
    private var hasEmittedStart = false
    private var isProcessingServiceMessage = false
    
    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (outputType == ProcessOutputTypes.STDOUT && !isProcessingServiceMessage) {
            val text = event.text
            
            // Skip if this is already a service message to prevent recursion
            if (text.startsWith("##teamcity[")) {
                return
            }
            
            // Buffer the output for parsing
            outputBuffer.append(text)
            processTestOutput(event, text)
        }
    }
    
    override fun processTerminated(event: ProcessEvent) {
        processCompleteOutput(event)
    }
    
    private fun processTestOutput(event: ProcessEvent, text: String) {
        val lines = text.lines()
        
        for (line in lines) {
            val trimmed = line.trim()
            
            // Emit initial service messages when we see test execution start
            if (!hasEmittedStart && trimmed.isNotEmpty() && 
                (trimmed.startsWith("Running tests in:") || !line.startsWith(" "))) {
                emitServiceMessage(event, "##teamcity[enteredTheMatrix]")
                emitServiceMessage(event, "##teamcity[testSuiteStarted name='TestCafe Tests']")
                hasEmittedStart = true
            }
            
            when {
                // Fixture detection - non-indented lines that aren't test results
                trimmed.isNotEmpty() && 
                !line.startsWith(" ") && 
                !trimmed.startsWith("√") &&
                !trimmed.startsWith("×") &&
                !trimmed.startsWith("Running tests in:") &&
                !trimmed.startsWith("-") &&
                !trimmed.contains("Hello from TestCafe!") &&
                !trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) &&
                !trimmed.startsWith("Warnings") &&
                !trimmed.contains(")") -> {
                    
                    // Close previous fixture if exists
                    currentFixture?.let { fixture ->
                        emitServiceMessage(event, "##teamcity[testSuiteFinished name='${escapeValue(fixture)}']")
                    }
                    
                    // Start new fixture
                    currentFixture = trimmed
                    emitServiceMessage(event, "##teamcity[testSuiteStarted name='${escapeValue(trimmed)}' locationHint='file://$testFilePath']")
                }
                
                // Passing test
                trimmed.startsWith("√") -> {
                    val testName = trimmed.substring(1).trim()
                    emitServiceMessage(event, "##teamcity[testStarted name='${escapeValue(testName)}' locationHint='file://$testFilePath']")
                    emitServiceMessage(event, "##teamcity[testFinished name='${escapeValue(testName)}' duration='0']")
                }
                
                // Failing test
                trimmed.startsWith("×") -> {
                    val testName = trimmed.substring(1).trim()
                    emitServiceMessage(event, "##teamcity[testStarted name='${escapeValue(testName)}' locationHint='file://$testFilePath']")
                    // We'll emit testFailed when we process the complete output
                }
            }
        }
        
        // Check if we've reached the end (summary line)
        if (text.contains(Regex("\\d+/\\d+\\s+failed"))) {
            processCompleteOutput(event)
        }
    }
    
    private fun processCompleteOutput(event: ProcessEvent) {
        val fullOutput = outputBuffer.toString()
        val lines = fullOutput.lines()
        
        var lastFailedTest: String? = null
        var collectingError = false
        val errorLines = mutableListOf<String>()
        
        for (line in lines) {
            val trimmed = line.trim()
            
            if (trimmed.startsWith("×")) {
                lastFailedTest = trimmed.substring(1).trim()
                collectingError = true
                errorLines.clear()
            } else if (collectingError && trimmed.matches(Regex("\\d+\\)\\s+.*"))) {
                errorLines.add(trimmed.substring(trimmed.indexOf(')') + 1).trim())
            } else if (collectingError && trimmed.isNotEmpty() && 
                      !trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) &&
                      !trimmed.startsWith("Warnings")) {
                errorLines.add(trimmed)
            } else if (collectingError && (trimmed.isEmpty() || 
                     trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) ||
                     trimmed.startsWith("Warnings"))) {
                // End of error section
                lastFailedTest?.let { testName ->
                    val errorMessage = errorLines.joinToString("\\n")
                    emitServiceMessage(event, "##teamcity[testFailed name='${escapeValue(testName)}' message='${escapeValue(errorMessage)}' details='${escapeValue(errorMessage)}']")
                    emitServiceMessage(event, "##teamcity[testFinished name='${escapeValue(testName)}' duration='0']")
                }
                collectingError = false
                lastFailedTest = null
                
                if (trimmed.matches(Regex("\\d+/\\d+\\s+failed.*"))) {
                    // Close remaining test suites
                    currentFixture?.let { fixture ->
                        emitServiceMessage(event, "##teamcity[testSuiteFinished name='${escapeValue(fixture)}']")
                    }
                    emitServiceMessage(event, "##teamcity[testSuiteFinished name='TestCafe Tests']")
                    break
                }
            }
        }
    }
    
    private fun emitServiceMessage(event: ProcessEvent, message: String) {
        isProcessingServiceMessage = true
        try {
            event.processHandler.notifyTextAvailable("$message\n", ProcessOutputTypes.STDOUT)
        } finally {
            isProcessingServiceMessage = false
        }
    }
    
    private fun escapeValue(value: String): String {
        return value
            .replace("|", "||")
            .replace("'", "|'")
            .replace("\n", "|n")
            .replace("\r", "|r")
            .replace("[", "|[")
            .replace("]", "|]")
    }
}