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
    private var collectingFailedTestMessages = false
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

        if (            //Collect error messages until next test or fixture
            collectingFailedTestMessages
            && trimmed.isNotBlank()
            && !trimmed.contains("Browser:")
            && !line.matches(Regex("\\s*\\d+\\).*"))
            && !line.matches(Regex("\\s*\\d+\\s\\|.*"))
            && !line.matches(Regex("\\s*>\\s\\d+\\s\\|.*"))
            && !trimmed.startsWith("at ")
        ) {
            failCurrentTestIfExists()
            collectingFailedTestMessages = false
        }

        when {
            collectingFailedTestMessages -> if (line.isNotBlank()) messages.add(line)
            // Test result lines: " √ testname" or " × testname"
            line.matches(Regex(" [√×] .*")) -> {
                val isSuccess = line.contains("√")
                val testName = line.substring(3).trim() // Remove " √ " or " × "

                if (collectingFailedTestMessages) {
                    // If we were collecting messages for a failed test, emit it now
                    failCurrentTestIfExists()
                    collectingFailedTestMessages = false
                }

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
                    collectingFailedTestMessages = true
                }
            }

            // Test run summary - finish up (detect various summary patterns)
            trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) ||
                    trimmed.matches(Regex("\\d+\\s+passed \\(.+\\)")) -> {
                if (collectingFailedTestMessages) {
                    // If we were collecting messages for a failed test, emit it now
                    failCurrentTestIfExists()
                    collectingFailedTestMessages = false
                }
                failCurrentTestIfExists()
                finishTestRun()
            }

            // Fixture lines: lines starting with single space but not test results
            line.startsWith(" ")
                    && !line.startsWith("  ")
                    && !line.matches(Regex(" [√×] .*"))
                    && trimmed != "--" -> {
                failCurrentTestIfExists()
                // Close previous fixture if exists
                currentFixture?.let { fixture ->
                    eventEmitter.emitFixtureFinished(fixture)
                }

                // Start new fixture
                currentFixture = trimmed
                eventEmitter.emitFixtureStarted(trimmed, testFilePath)
            }

            // Default case: collect any other line as a message if we have a fixture context
            else -> {
                if (trimmed.isNotBlank()) messages.add(line)
            }
        }
    }

    /**
     * Call this when parsing is complete to ensure all tests are properly finished
     */
    fun finalizeParsing() {
        finishTestRun()
    }

    private fun failCurrentTestIfExists() {
        currentTest?.let { testName ->
            val collectedMessages = if (messages.isNotEmpty()) messages.joinToString("\n") else "Test failed"
            eventEmitter.emitTestFailed(testName, collectedMessages)
            messages.clear()
            currentTest = null
        }
    }

    private fun finishTestRun() {
        // If we still have a failed test in progress, emit it now
        currentTest?.let { testName ->
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
