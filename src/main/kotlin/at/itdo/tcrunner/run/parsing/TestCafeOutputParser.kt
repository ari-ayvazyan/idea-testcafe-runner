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
    private var currentFailedTest: String? = null
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
            // Passing test
            trimmed.startsWith("√") -> {
                val testName = trimmed.substring(1).trim()
                eventEmitter.emitTestStarted(testName, testFilePath)
                eventEmitter.emitTestPassed(testName)
            }

            // Failing test - start collecting error details
            trimmed.startsWith("×") -> {
                val testName = trimmed.substring(1).trim()
                eventEmitter.emitTestStarted(testName, testFilePath)

                // Start collecting error details for this test
                currentFailedTest = testName
            }

            // Fixture lines - specifically look for lines that match fixture pattern
            line.startsWith(" ")
                    && !line.startsWith("  ")
                    && !line.startsWith(" --")
                        -> {

                // Close previous fixture if exists
                currentFixture?.let { fixture ->
                    eventEmitter.emitFixtureFinished(fixture)
                }

                // Start new fixture
                currentFixture = trimmed
                eventEmitter.emitFixtureStarted(trimmed, testFilePath)
            }

            // Test run summary - finish up
            trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) ||
            trimmed.matches(Regex("\\d+\\s+passed \\(.+\\)")) -> {
                finishTestRun()
            }
        }
    }

    private fun finishTestRun() {
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
    fun emitTestPassed(testName: String)
    fun emitTestFailed(testName: String, errorMessage: String)
}
