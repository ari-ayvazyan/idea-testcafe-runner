package at.itdo.tcrunner.run.parsing

/**
 * Pure parser for TestCafe output that extracts test events without IntelliJ dependencies
 */
class TestCafeOutputParser(
    private val testFilePath: String,
    private val eventEmitter: TestEventEmitter
) {
    private val lineClassifier = TestCafeLineClassifier()
    private val messageCollector = MessageCollector()

    private var currentFixture: String? = null
    private var hasEmittedStart = false
    private var currentTest: String? = null
    private var collectingFailedTestMessages = false

    fun processLine(line: String) {
        val lineType = lineClassifier.classifyLine(line)

        handleTestExecutionStart(lineType)
        handleErrorMessageContext(line, lineType)

        when (lineType) {
            LineType.TEST_RESULT -> handleTestResult(line)
            LineType.TEST_RUN_SUMMARY -> handleTestRunSummary()
            LineType.FIXTURE -> handleFixture(line)
            LineType.ERROR_MESSAGE -> {
                if (collectingFailedTestMessages) {
                    handleMessage(line)
                }
            }
            LineType.REGULAR_MESSAGE -> {
                handleMessage(line)
            }
            LineType.EMPTY, LineType.TEST_EXECUTION_START -> {
                // Only collect non-blank lines as messages
                if (line.isNotBlank()) {
                    handleMessage(line)
                }
            }
        }
    }

    /**
     * Call this when parsing is complete to ensure all tests are properly finished
     */
    fun finalizeParsing() {
        finishTestRun()
    }

    private fun handleTestExecutionStart(lineType: LineType) {
        if (!hasEmittedStart && lineType == LineType.TEST_EXECUTION_START) {
            eventEmitter.emitTestRunStarted()
            hasEmittedStart = true
        }
    }

    private fun handleErrorMessageContext(line: String, lineType: LineType) {
        // The original logic was: if we're collecting failed test messages and we encounter
        // a line that matches the error pattern, we should finish the current failed test
        if (collectingFailedTestMessages && shouldStopCollectingFailedTestMessages(line)) {
            failCurrentTestIfExists()
            collectingFailedTestMessages = false
        }
    }

    private fun shouldStopCollectingFailedTestMessages(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.isNotBlank() &&
               !trimmed.contains("Browser:") &&
               !line.matches(Regex("\\s*\\d+\\).*")) &&
               !line.matches(Regex("\\s*\\d+\\s\\|.*")) &&
               !line.matches(Regex("\\s*>\\s\\d+\\s\\|.*")) &&
               !trimmed.startsWith("at ")
    }

    private fun handleTestResult(line: String) {
        val testResult = lineClassifier.parseTestResult(line) ?: return

        finishPreviousFailedTestIfCollecting()

        eventEmitter.emitTestStarted(testResult.testName, testFilePath)

        if (testResult.isSuccess) {
            handlePassedTest(testResult.testName)
        } else {
            handleFailedTest(testResult.testName)
        }
    }

    private fun handlePassedTest(testName: String) {
        val collectedMessages = messageCollector.getCollectedMessages()
        eventEmitter.emitTestPassed(testName, collectedMessages)
        messageCollector.clear()
        currentTest = null
        collectingFailedTestMessages = false
    }

    private fun handleFailedTest(testName: String) {
        currentTest = testName
        collectingFailedTestMessages = true
    }

    private fun handleTestRunSummary() {
        finishPreviousFailedTestIfCollecting()
        failCurrentTestIfExists()
        finishTestRun()
    }

    private fun handleFixture(line: String) {
        val fixtureName = lineClassifier.parseFixtureName(line) ?: return

        failCurrentTestIfExists()
        closeCurrentFixtureIfExists()

        currentFixture = fixtureName
        eventEmitter.emitFixtureStarted(fixtureName, testFilePath)
    }

    private fun handleMessage(line: String) {
        messageCollector.addMessage(line)
    }

    private fun finishPreviousFailedTestIfCollecting() {
        if (collectingFailedTestMessages) {
            failCurrentTestIfExists()
            collectingFailedTestMessages = false
        }
    }

    private fun failCurrentTestIfExists() {
        currentTest?.let { testName ->
            val collectedMessages = messageCollector.getCollectedMessagesOrDefault("Test failed")
            eventEmitter.emitTestFailed(testName, collectedMessages)
            messageCollector.clear()
            currentTest = null
            collectingFailedTestMessages = false
        }
    }

    private fun closeCurrentFixtureIfExists() {
        currentFixture?.let { fixture ->
            eventEmitter.emitFixtureFinished(fixture)
            currentFixture = null
        }
    }

    private fun finishTestRun() {
        // If we still have a failed test in progress, emit it now
        currentTest?.let { testName ->
            val collectedMessages = messageCollector.getCollectedMessagesOrDefault("Test failed")
            eventEmitter.emitTestPassed(testName, collectedMessages)
            messageCollector.clear()
            currentTest = null
            collectingFailedTestMessages = false
        }

        closeCurrentFixtureIfExists()
        eventEmitter.emitTestRunFinished()
    }

}

