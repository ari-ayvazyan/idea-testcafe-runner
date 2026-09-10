package at.itdo.tcrunner

import at.itdo.tcrunner.run.parsing.ProcessOutputHandler
import at.itdo.tcrunner.run.parsing.TeamCityEventEmitter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class ProcessOutputHandlerTest {

    private lateinit var processHandler: ProcessHandler
    private lateinit var processEvent: ProcessEvent
    private lateinit var capturedOutput: MutableList<String>
    private val testFilePath = "/path/to/project/testcafe/tests/simple.test.js"

    @BeforeEach
    fun setUp() {
        capturedOutput = mutableListOf()
        processHandler = mockk<ProcessHandler>()
        processEvent = mockk<ProcessEvent>()

        every { processEvent.processHandler } returns processHandler

        // Capture all TeamCity service messages
        val messageSlot = slot<String>()
        every { processHandler.notifyTextAvailable(capture(messageSlot), ProcessOutputTypes.STDOUT) } answers {
            capturedOutput.add(messageSlot.captured.trim())
        }
    }

    @Test
    fun `should process complete TestCafe output and emit all TeamCity messages`() {
        println("=== ProcessOutputHandler Test: Complete TestCafe Output Processing ===")

        val eventEmitter = TeamCityEventEmitter()
        val handler = ProcessOutputHandler(testFilePath, eventEmitter)

        eventEmitter.setCurrentProcessEvent(processEvent)

        // Read the sample test output file
        val sampleFile = File("src/test/resources/tc-sample-test-with-failure.txt")
        val sampleOutput = sampleFile.readText()

        println("\n--- Input TestCafe Output ---")
        sampleOutput.lines().forEachIndexed { index, line ->
            println("${(index + 1).toString().padStart(3)}: $line")
        }

        println("\n--- Processing Output Through ProcessOutputHandler ---")

        // Process each line through the handler
        sampleOutput.lines().forEach { line ->
            every { processEvent.text } returns "$line\n"
            handler.onTextAvailable(processEvent, ProcessOutputTypes.STDOUT)
        }

        // Finalize processing
        handler.processTerminated(processEvent)

        println("\n--- Generated TeamCity Service Messages ---")
        capturedOutput.forEachIndexed { index, message ->
            println("${(index + 1).toString().padStart(3)}: $message")
        }

        println("\n--- Analysis of Generated Messages ---")
        val testRunStarted = capturedOutput.count { it.contains("##teamcity[enteredTheMatrix]") }
        val testSuiteStarted = capturedOutput.count { it.contains("##teamcity[testSuiteStarted") }
        val testStarted = capturedOutput.count { it.contains("##teamcity[testStarted") }
        val testPassed = capturedOutput.count { it.contains("##teamcity[testFinished") && !it.contains("testFailed") }
        val testFailed = capturedOutput.count { it.contains("##teamcity[testFailed") }
        val testFinished = capturedOutput.count { it.contains("##teamcity[testFinished") }
        val testStdOut = capturedOutput.count { it.contains("##teamcity[testStdOut") }
        val testSuiteFinished = capturedOutput.count { it.contains("##teamcity[testSuiteFinished") }

        println("Test Run Started: $testRunStarted")
        println("Test Suites Started: $testSuiteStarted")
        println("Tests Started: $testStarted")
        println("Tests Passed: ${testFinished - testFailed}")
        println("Tests Failed: $testFailed")
        println("Test StdOut Messages: $testStdOut")
        println("Test Suites Finished: $testSuiteFinished")
        println("Total Messages: ${capturedOutput.size}")

        // Validate key expectations
        val enteredTheMatrix = capturedOutput.count { it.contains("##teamcity[enteredTheMatrix]") }
        assert(enteredTheMatrix > 0) { "Should have test run started message (enteredTheMatrix)" }
        assert(testStarted > 0) { "Should have test started messages" }
        assert(testFailed > 0) { "Should have test failed messages (sample contains failures)" }
        assert(testFinished > 0) { "Should have test finished messages" }

        println("\n--- Test Names Extracted ---")
        capturedOutput.filter { it.contains("##teamcity[testStarted") }
            .forEach { message ->
                val testNameMatch = Regex("name='([^']*)'").find(message)
                testNameMatch?.let {
                    println("Test: ${it.groupValues[1]}")
                }
            }

        println("\n--- Failed Test Details ---")
        capturedOutput.filter { it.contains("##teamcity[testFailed") }
            .forEach { message ->
                val testNameMatch = Regex("name='([^']*)'").find(message)
                val messageMatch = Regex("message='([^']*)'").find(message)
                testNameMatch?.let { nameMatch ->
                    messageMatch?.let { msgMatch ->
                        println("Failed Test: ${nameMatch.groupValues[1]}")
                        println("  Error: ${msgMatch.groupValues[1]}")
                    }
                }
            }

        println("\n=== Test Completed Successfully ===")
    }
}