package at.itdo.tcrunner.run.parsing

/**
 * Pure parser for TestCafe output that extracts test events without IntelliJ dependencies
 */
class TestCafeOutputParser(
    private val testFilePath: String,
    private val eventEmitter: TestEventEmitter
) {
    private var currentFixture: String? = null
    private var hasEmittedStart = false
    private var currentTest: String? = null
    private var messages: MutableList<String> = mutableListOf()

    fun processLine(line: String) {
        val trimmed = line.trim()

        // Emit initial events when we see test execution start
        if (!hasEmittedStart && trimmed.isNotEmpty() &&
            (trimmed.startsWith("Running tests in:") || !line.startsWith(" "))
        ) {
            eventEmitter.emitTestRunStarted()
            hasEmittedStart = true
        }

        when {
            // Test result lines: " √ testname" or " × testname"
            line.matches(Regex(" [√×] .*")) -> {
                val isSuccess = line.contains("√")
                val testName = line.substring(3).trim() // Remove " √ " or " × "

                eventEmitter.emitTestStarted(testName, testFilePath)

                if (isSuccess) {
                    // Emit collected messages with the passed test
                    val collectedMessages = if (messages.isNotEmpty()) messages.joinToString("\n") else ""
                    eventEmitter.emitTestPassed(testName, collectedMessages)
                    messages.clear() // Clear messages after passing test
                    currentTest = null
                } else {
                    // For failed test, keep all accumulated messages and start collecting more
                    currentTest = testName
                }
            }

            // Fixture lines: lines starting with single space but not test results
            line.startsWith(" ")
                    && !line.startsWith("  ")
                    && !line.matches(Regex(" [√×] .*"))
                    && trimmed != "--" -> {
                finishCurrentTest()
                // Close previous fixture if exists
                currentFixture?.let { fixture ->
                    eventEmitter.emitFixtureFinished(fixture)
                }

                // Start new fixture
                currentFixture = trimmed
                eventEmitter.emitFixtureStarted(trimmed, testFilePath)
            }

            // Test run summary - finish up (detect various summary patterns)
            trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) ||
                    trimmed.matches(Regex("\\d+\\s+passed \\(.+\\)")) -> {
                finishCurrentTest()
                finishTestRun()
            }

            // Default case: collect any other line as a message if we have a fixture context
            else -> {
                if (trimmed.isNotBlank()) messages.add(line)
            }
        }
    }

    private fun finishCurrentTest() {
        currentTest?.let { testName ->
            val collectedMessages = if (messages.isNotEmpty()) messages.joinToString("\n") else "Test failed"
            eventEmitter.emitTestFailed(testName, collectedMessages)
            messages.clear()
            currentTest = null
        }
    }

    private fun finishTestRun() {
        // If we still have a failed test in progress, emit it now
        getCurrentTestnameOrCreateDefaultIfLogsPresent()?.let { testName ->
            val collectedMessages = if (messages.isNotEmpty()) messages.joinToString("\n") else "Test failed"
            eventEmitter.emitTestPassed(testName, collectedMessages)
            messages.clear()
            currentTest = null
        }

        // Close remaining fixture and test run
        currentFixture?.let { fixture ->
            eventEmitter.emitFixtureFinished(fixture)
        }
        eventEmitter.emitTestRunFinished()
    }

    private fun getCurrentTestnameOrCreateDefaultIfLogsPresent(): String? {
        if (currentTest != null || messages.isNotEmpty()) return currentTest
        eventEmitter.emitTestStarted("logs", testFilePath)
        currentTest = "logs"
        return "logs"
    }
}

/**
 * Interface for emitting test events, allowing different implementations for testing vs production
 */
interface TestEventEmitter {
    fun emitTestRunStarted()
    fun emitTestRunFinished()
    fun emitFixtureStarted(fixtureName: String, filePath: String)
    fun emitFixtureFinished(fixtureName: String)
    fun emitTestStarted(testName: String, filePath: String)
    fun emitTestPassed(testName: String, messages: String = "")
    fun emitTestFailed(testName: String, messages: String = "")
}
