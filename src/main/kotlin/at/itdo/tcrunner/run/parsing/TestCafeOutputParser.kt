package at.itdo.tcrunner.run.parsing

/**
 * Pure parser for TestCafe output that extracts test events without IntelliJ dependencies
 */
class TestCafeOutputParser(
    private val testFilePath: String,
    private val eventEmitter: TestEventEmitter
) {
    private val outputBuffer = StringBuilder()
    private var currentFixture: String? = null
    private var hasEmittedStart = false

    fun processLine(line: String) {
        outputBuffer.append(line).append("\n")

        val trimmed = line.trim()

        // Emit initial events when we see test execution start
        if (!hasEmittedStart && trimmed.isNotEmpty() &&
            (trimmed.startsWith("Running tests in:") || !line.startsWith(" "))) {
            eventEmitter.emitTestRunStarted()
            hasEmittedStart = true
        }

        when {
            // Fixture detection - lines that contain fixture names
            trimmed.isNotEmpty() &&
            !trimmed.startsWith("√") &&
            !trimmed.startsWith("×") &&
            !trimmed.startsWith("Running tests in:") &&
            !trimmed.startsWith("Testing started at") &&
            !trimmed.startsWith("bash -c") &&
            !trimmed.startsWith("-") &&
            !trimmed.contains("Hello from TestCafe!") &&
            !trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) &&
            !trimmed.startsWith("Warnings") &&
            !trimmed.contains(")") &&
            !trimmed.startsWith("Chrome ") &&
            // Fixture lines are either non-indented or slightly indented (like " Sample Test")
            ((!line.startsWith(" ")) || (line.startsWith(" ") && !line.startsWith("  "))) -> {

                // Close previous fixture if exists
                currentFixture?.let { fixture ->
                    eventEmitter.emitFixtureFinished(fixture)
                }

                // Start new fixture
                currentFixture = trimmed
                eventEmitter.emitFixtureStarted(trimmed, testFilePath)
            }

            // Passing test
            trimmed.startsWith("√") -> {
                val testName = trimmed.substring(1).trim()
                eventEmitter.emitTestStarted(testName, testFilePath)
                eventEmitter.emitTestPassed(testName)
            }

            // Failing test
            trimmed.startsWith("×") -> {
                val testName = trimmed.substring(1).trim()
                eventEmitter.emitTestStarted(testName, testFilePath)
                // We'll emit testFailed when we process the complete output
            }
        }

        // Check if we've reached the end (summary line)
        if (line.contains(Regex("\\d+/\\d+\\s+failed"))) {
            processCompleteOutput()
        }
    }

    fun processCompleteOutput() {
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
                    val errorMessage = errorLines.joinToString("\n")
                    eventEmitter.emitTestFailed(testName, errorMessage)
                }
                collectingError = false
                lastFailedTest = null

                if (trimmed.matches(Regex("\\d+/\\d+\\s+failed.*"))) {
                    // Close remaining test suites
                    currentFixture?.let { fixture ->
                        eventEmitter.emitFixtureFinished(fixture)
                    }
                    eventEmitter.emitTestRunFinished()
                    break
                }
            }
        }
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
