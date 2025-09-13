package at.itdo.tcrunner

import at.itdo.tcrunner.run.parsing.TestCafeOutputParser
import at.itdo.tcrunner.run.parsing.TestEventEmitter
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TimingIntegrationTest {

    @Test
    fun `should parse and emit timing information from TestCafe output`() {
        val eventCollector = TimingEventCollector()
        val parser = TestCafeOutputParser("test.js", eventCollector)

        // Simulate TestCafe output with timing information
        val testCafeOutput = listOf(
            "Running tests in:",
            " - Chrome 140.0.0.0 / Windows 11",
            "",
            " Sample Test",
            " √ Simple test with console log",
            " √ Simple test 2 with console log",
            " × Simple test 3 with err",
            "",
            " 1/3 failed (2s)",
            ""
        )

        testCafeOutput.forEach { line ->
            parser.processLine(line)
        }
        parser.finalizeParsing()

        // Verify timing information was captured
        assertTrue(eventCollector.testRunFinishedWithDuration, "Test run should have finished with duration")
        assertTrue(eventCollector.capturedDuration.isNotEmpty(), "Should have captured overall duration")
        assertTrue(eventCollector.capturedDuration == "2s", "Should capture correct duration: ${eventCollector.capturedDuration}")
    }

    private class TimingEventCollector : TestEventEmitter {
        var testRunFinishedWithDuration = false
        var capturedDuration = ""

        override fun emitTestRunStarted() {}
        override fun emitTestRunFinished(duration: String) {
            testRunFinishedWithDuration = true
            capturedDuration = duration
        }
        override fun emitFixtureStarted(fixtureName: String, filePath: String) {}
        override fun emitFixtureFinished(fixtureName: String, duration: String) {}
        override fun emitTestStarted(testName: String, filePath: String) {}
        override fun emitTestPassed(testName: String, messages: String, duration: String) {}
        override fun emitTestFailed(testName: String, messages: String, duration: String) {}
    }
}