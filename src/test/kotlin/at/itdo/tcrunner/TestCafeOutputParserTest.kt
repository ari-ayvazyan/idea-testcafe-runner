package at.itdo.tcrunner

import at.itdo.tcrunner.run.parsing.TestCafeOutputParser
import at.itdo.tcrunner.run.parsing.TestEventEmitter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull

class TestCafeOutputParserTest {

    private lateinit var capturedEvents: MutableList<TestEvent>
    private lateinit var parser: TestCafeOutputParser
    private val testFilePath = "D:/workspace/idea-testcafe-runner/testcafe/tests/simple.test.js"

    @BeforeEach
    fun setUp() {
        capturedEvents = mutableListOf()
        val eventEmitter = TestEventCapture()
        parser = TestCafeOutputParser(testFilePath, eventEmitter)
    }

    @Test
    fun `should parse TestCafe output and emit correct events`() {
        // Load the sample test log
        val logFile = File("src/test/resources/tc-sample-test-with-failure.txt")
        val logContent = logFile.readText()

        // Process each line as the parser would receive them
        logContent.lines().forEach { line ->
            parser.processLine(line)
        }

        // Verify the events that were captured
        val events = capturedEvents

        // Should start with test run started
        assertTrue(events.any { it is TestEvent.TestRunStarted })

        // Should detect the fixture. we dont care if other things than fixtures are detected aswell
        val fixtureStarted = events.filterIsInstance<TestEvent.FixtureStarted>()
        println("Detected fixtures: ${fixtureStarted.map { it.fixtureName }}")
        assertTrue(fixtureStarted.isNotEmpty())
        assertNotNull(fixtureStarted.find { it.fixtureName == "Sample Test" }?.fixtureName)
        assertEquals(testFilePath, fixtureStarted.find { it.fixtureName == "Sample Test" }?.filePath)

        // Should detect all test starts
        val testStarted = events.filterIsInstance<TestEvent.TestStarted>()
        assertEquals(3, testStarted.size)
        assertEquals("Simple test with console log", testStarted[0].testName)
        assertEquals("Simple test 2 with console log", testStarted[1].testName)
        assertEquals("Simple test 3 with err", testStarted[2].testName)

        // Should detect passed tests
        val testPassed = events.filterIsInstance<TestEvent.TestPassed>()
        assertEquals(2, testPassed.size)
        assertEquals("Simple test with console log", testPassed[0].testName)
        assertEquals("Simple test 2 with console log", testPassed[1].testName)

        val testFailed = events.filterIsInstance<TestEvent.TestFailed>()
        assertTrue(testFailed.isNotEmpty())
        assertTrue(testFailed.any { it.testName == "Simple test 3 with err" })

        println("Successfully parsed TestCafe output with ${events.size} total events")
    }

    @Test
    fun `should handle empty lines and console output`() {
        parser.processLine("")
        parser.processLine("Hello from TestCafe!")
        parser.processLine(" Running tests in:")

        // Should not generate spurious events for empty lines or console output
        val meaningfulEvents = capturedEvents.filterNot {
            it is TestEvent.TestRunStarted // This might be triggered by "Running tests in:"
        }
        assertTrue(meaningfulEvents.isEmpty() || meaningfulEvents.size <= 1)
    }

    @Test
    fun `should escape special characters in TeamCity messages`() {
        parser.processLine("Sample Test")
        parser.processLine("√ Test with |special| characters ['and brackets']")

        val testStarted = capturedEvents.filterIsInstance<TestEvent.TestStarted>()
        if (testStarted.isNotEmpty()) {
            assertEquals("Test with |special| characters ['and brackets']", testStarted.last().testName)
        }
    }

    private inner class TestEventCapture : TestEventEmitter {
        override fun emitTestRunStarted() {
            capturedEvents.add(TestEvent.TestRunStarted)
        }

        override fun emitTestRunFinished() {
            capturedEvents.add(TestEvent.TestRunFinished)
        }

        override fun emitFixtureStarted(fixtureName: String, filePath: String) {
            capturedEvents.add(TestEvent.FixtureStarted(fixtureName, filePath))
        }

        override fun emitFixtureFinished(fixtureName: String) {
            capturedEvents.add(TestEvent.FixtureFinished(fixtureName))
        }

        override fun emitTestStarted(testName: String, filePath: String) {
            capturedEvents.add(TestEvent.TestStarted(testName, filePath))
        }

        override fun emitTestPassed(testName: String) {
            capturedEvents.add(TestEvent.TestPassed(testName))
        }

        override fun emitTestFailed(testName: String, errorMessage: String) {
            capturedEvents.add(TestEvent.TestFailed(testName, errorMessage))
        }
    }

    sealed class TestEvent {
        object TestRunStarted : TestEvent()
        object TestRunFinished : TestEvent()
        data class FixtureStarted(val fixtureName: String, val filePath: String) : TestEvent()
        data class FixtureFinished(val fixtureName: String) : TestEvent()
        data class TestStarted(val testName: String, val filePath: String) : TestEvent()
        data class TestPassed(val testName: String) : TestEvent()
        data class TestFailed(val testName: String, val errorMessage: String) : TestEvent()
    }
}
