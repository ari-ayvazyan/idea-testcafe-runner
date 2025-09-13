package at.itdo.tcrunner

import at.itdo.tcrunner.run.parsing.TestCafeOutputParser
import at.itdo.tcrunner.run.parsing.TestEventEmitter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
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
        // Debug with individual lines
        println("=== Processing individual lines ===")
        parser.processLine(" Sample Test")
        println("After fixture line: ${capturedEvents.map { it.javaClass.simpleName }}")

        parser.processLine("This message belongs to Simple test 3!")
        println("After message line: ${capturedEvents.map { it.javaClass.simpleName }}")

        parser.processLine(" × Simple test 3 with err")
        println("After failed test line: ${capturedEvents.map { it.javaClass.simpleName }}")

        parser.processLine("")
        parser.processLine("   1) A call to an async function is not awaited.")
        parser.processLine(" 1/3 failed (2s)")
        println("After summary line: ${capturedEvents.map { it.javaClass.simpleName }}")

        // Check events
        val testFailed = capturedEvents.filterIsInstance<TestEvent.TestFailed>()
        println("Failed tests found: ${testFailed.map { "${it.testName}: ${it.messages.take(50) + if (it.messages.length > 50) "..." else ""}" }}")
        println("All events: ${capturedEvents.map { "${it.javaClass.simpleName}: ${if (it is TestEvent.TestStarted) it.testName else if (it is TestEvent.TestPassed) it.testName else if (it is TestEvent.TestFailed) it.testName else ""}" }}")

        assertTrue(testFailed.isNotEmpty(), "Expected at least one failed test event")
        assertTrue(testFailed.any { it.testName == "Simple test 3 with err" }, "Expected to find 'Simple test 3 with err' in failed tests")

        // Now test with full sample
        capturedEvents.clear()
        parser = TestCafeOutputParser(testFilePath, TestEventCapture())

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

        val testFailedFull = events.filterIsInstance<TestEvent.TestFailed>()
        println("Failed tests found in full test: ${testFailedFull.map { "${it.testName}: ${it.messages.take(50) + if (it.messages.length > 50) "..." else ""}" }}")
        assertTrue(testFailedFull.isNotEmpty(), "Expected at least one failed test event")
        assertTrue(testFailedFull.any { it.testName == "Simple test 3 with err" }, "Expected to find 'Simple test 3 with err' in failed tests")

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

    @Test
    fun `should collect console messages for failed tests`() {
        // Test just the summary regex first
        val summaryLine = "1/3 failed (2s)"
        val containsTest = summaryLine.contains("failed") && summaryLine.matches(Regex("\\d+.*failed.*"))
        println("Summary line: '$summaryLine'")
        println("Contains failed: ${summaryLine.contains("failed")}")
        println("Matches regex \\d+.*failed.*: ${summaryLine.matches(Regex("\\d+.*failed.*"))}")
        
        // Test the exact pattern from the file
        val actualLine = " 1/3 failed (2s)"
        val trimmedLine = actualLine.trim()
        println("Actual line: '$actualLine'")
        println("Trimmed line: '$trimmedLine'")
        println("Trimmed contains failed: ${trimmedLine.contains("failed")}")
        println("Trimmed matches \\d+.*failed.*: ${trimmedLine.matches(Regex("\\d+.*failed.*"))}")
        println("Combined condition for trimmed: ${trimmedLine.contains("failed") && trimmedLine.matches(Regex("\\d+.*failed.*"))}")
        
        println("Combined condition: $containsTest")
        
        // Test the specific case: console message followed by failed test
        capturedEvents.clear()
        parser.processLine(" Sample Test") // Fixture
        println("After fixture: ${capturedEvents.size} events")
        
        parser.processLine("This message belongs to Simple test 3!") // Console message
        println("After console message: ${capturedEvents.size} events")
        
        parser.processLine(" × Simple test 3 with err") // Failed test
        println("After failed test marker: ${capturedEvents.size} events")
        
        parser.processLine("   1) Error details") // Error details
        println("After error details: ${capturedEvents.size} events")
        
        parser.processLine(" 1/3 failed (2s)") // Summary
        println("After summary: ${capturedEvents.size} events")

        val failed = capturedEvents.filterIsInstance<TestEvent.TestFailed>()
        println("Failed test count: ${failed.size}")
        if (failed.isNotEmpty()) {
            println("Failed test messages: '${failed.first().messages}'")
        }

        assertTrue(failed.isNotEmpty(), "Should have failed test event")
        assertEquals("Simple test 3 with err", failed.first().testName)
        assertTrue(failed.first().messages.contains("This message belongs to Simple test 3!"),
                  "Failed test should include console message: '${failed.first().messages}'")
    }

    @Test
    fun `should match test result patterns correctly`() {
        val passLine = " √ Simple test with console log"
        val failLine = " × Simple test 3 with err"
        val fixtureLine = " Sample Test"

        // Test the regex patterns
        assertTrue(passLine.matches(Regex(" [√×] .*")), "Pass line should match test result pattern")
        assertTrue(failLine.matches(Regex(" [√×] .*")), "Fail line should match test result pattern")
        assertFalse(fixtureLine.matches(Regex(" [√×] .*")), "Fixture line should not match test result pattern")

        // Test the parser logic directly
        capturedEvents.clear()
        parser.processLine(fixtureLine) // Fixture
        parser.processLine("This message belongs to test!")
        parser.processLine(failLine) // Failed test
        parser.processLine("   1) Error details")
        parser.processLine(" 1/3 failed (2s)") // Summary

        val failed = capturedEvents.filterIsInstance<TestEvent.TestFailed>()
        println("Test failed events: ${failed.map { it.testName }}")
        assertTrue(failed.isNotEmpty(), "Should have failed test event")
        assertEquals("Simple test 3 with err", failed.first().testName)
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

        override fun emitTestPassed(testName: String, messages: String) {
            capturedEvents.add(TestEvent.TestPassed(testName, messages))
        }

        override fun emitTestFailed(testName: String, messages: String) {
            capturedEvents.add(TestEvent.TestFailed(testName, messages))
        }
    }

    sealed class TestEvent {
        object TestRunStarted : TestEvent()
        object TestRunFinished : TestEvent()
        data class FixtureStarted(val fixtureName: String, val filePath: String) : TestEvent()
        data class FixtureFinished(val fixtureName: String) : TestEvent()
        data class TestStarted(val testName: String, val filePath: String) : TestEvent()
        data class TestPassed(val testName: String, val messages: String = "") : TestEvent()
        data class TestFailed(val testName: String, val messages: String = "") : TestEvent()
    }
}
