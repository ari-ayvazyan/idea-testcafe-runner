package at.itdo.tcrunner.run.parsing

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

    private var isProcessingServiceMessage = false
    private var currentProcessEvent: ProcessEvent? = null
    private lateinit var parser: TestCafeOutputParser

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (outputType == ProcessOutputTypes.STDOUT && !isProcessingServiceMessage) {
            val text = event.text
            currentProcessEvent = event

            // Initialize parser with event emitter that has access to ProcessEvent
            if (!::parser.isInitialized) {
                parser = TestCafeOutputParser(testFilePath, IntellijTestEventEmitter())
            }

            // Skip if this is already a service message to prevent recursion
            if (text.startsWith("##teamcity[")) {
                return
            }

            // Process each line
            text.lines().forEach { line ->
                parser.processLine(line)
            }
        }
    }

    override fun processTerminated(event: ProcessEvent) {
        // When the process ends, finalize parsing to ensure all tests are properly finished
        if (::parser.isInitialized) {
            parser.finalizeParsing()
        }
    }

    private fun emitServiceMessage(message: String) {
        isProcessingServiceMessage = true
        try {
            currentProcessEvent?.processHandler?.notifyTextAvailable("$message\n", ProcessOutputTypes.STDOUT)
        } finally {
            isProcessingServiceMessage = false
        }
    }

    /**
     * Implementation of TestEventEmitter that emits TeamCity service messages
     */
    private inner class IntellijTestEventEmitter : TestEventEmitter {
        override fun emitTestRunStarted() {
            emitServiceMessage(TeamCityServiceMessageFormatter.testRunStarted())
            emitServiceMessage(TeamCityServiceMessageFormatter.testSuiteStarted("TestCafe Tests"))
        }

        override fun emitTestRunFinished() {
            emitServiceMessage(TeamCityServiceMessageFormatter.testSuiteFinished("TestCafe Tests"))
        }

        override fun emitFixtureStarted(fixtureName: String, filePath: String) {
            emitServiceMessage(TeamCityServiceMessageFormatter.testSuiteStarted(fixtureName, "file://$filePath"))
        }

        override fun emitFixtureFinished(fixtureName: String) {
            emitServiceMessage(TeamCityServiceMessageFormatter.testSuiteFinished(fixtureName))
        }

        override fun emitTestStarted(testName: String, filePath: String) {
            emitServiceMessage(TeamCityServiceMessageFormatter.testStarted(testName, "file://$filePath"))
        }

        override fun emitTestPassed(testName: String, messages: String) {
            // Include messages in test output if present
            if (messages.isNotEmpty()) {
                emitServiceMessage(TeamCityServiceMessageFormatter.testStdOut(testName, messages))
            }
            emitServiceMessage(TeamCityServiceMessageFormatter.testFinished(testName))
        }

        override fun emitTestFailed(testName: String, messages: String) {
            // Use the messages as both console output and error details
            if (messages.isNotEmpty()) {
                emitServiceMessage(TeamCityServiceMessageFormatter.testFailed(testName, messages))
            }
            emitServiceMessage(TeamCityServiceMessageFormatter.testFinished(testName))
        }
    }
}
