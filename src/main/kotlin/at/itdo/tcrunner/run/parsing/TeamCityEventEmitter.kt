package at.itdo.tcrunner.run.parsing

import at.itdo.tcrunner.run.parsing.TeamCityServiceMessageFormatter.testFailed
import at.itdo.tcrunner.run.parsing.TeamCityServiceMessageFormatter.testFinished
import at.itdo.tcrunner.run.parsing.TeamCityServiceMessageFormatter.testRunStarted
import at.itdo.tcrunner.run.parsing.TeamCityServiceMessageFormatter.testStarted
import at.itdo.tcrunner.run.parsing.TeamCityServiceMessageFormatter.testStdOut
import at.itdo.tcrunner.run.parsing.TeamCityServiceMessageFormatter.testSuiteFinished
import at.itdo.tcrunner.run.parsing.TeamCityServiceMessageFormatter.testSuiteStarted
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes

/**
 * Emits TeamCity service messages for test events
 */
class TeamCityEventEmitter : TestEventEmitter {
    private var isProcessingServiceMessage = false
    private var currentProcessEvent: ProcessEvent? = null
    private val testStartTimes = mutableMapOf<String, Long>()
    private val fixtureStartTimes = mutableMapOf<String, Long>()
    private var testRunStartTime: Long = 0

    fun setCurrentProcessEvent(event: ProcessEvent) {
        currentProcessEvent = event
    }

    fun isProcessingServiceMessage() = isProcessingServiceMessage

    override fun emitTestRunStarted() {
        testRunStartTime = System.currentTimeMillis()
        emitServiceMessage(testRunStarted())
        emitServiceMessage(testSuiteStarted("TestCafe Tests"))
    }

    override fun emitTestRunFinished(duration: String) {
        val calculatedDuration = if (duration.isNotEmpty()) {
            convertDurationToMs(duration)
        } else if (testRunStartTime > 0) {
            (System.currentTimeMillis() - testRunStartTime).toString()
        } else "0"

        emitServiceMessage(testSuiteFinished("TestCafe Tests"))
    }

    override fun emitFixtureStarted(fixtureName: String, filePath: String) {
        fixtureStartTimes[fixtureName] = System.currentTimeMillis()
        emitServiceMessage(testSuiteStarted(fixtureName, "file://$filePath"))
    }

    override fun emitFixtureFinished(fixtureName: String, duration: String) {
        val calculatedDuration = if (duration.isNotEmpty()) {
            convertDurationToMs(duration)
        } else {
            val startTime = fixtureStartTimes[fixtureName]
            if (startTime != null) {
                (System.currentTimeMillis() - startTime).toString()
            } else "0"
        }

        fixtureStartTimes.remove(fixtureName)
        emitServiceMessage(testSuiteFinished(fixtureName))
    }

    override fun emitTestStarted(testName: String, filePath: String) {
        testStartTimes[testName] = System.currentTimeMillis()
        emitServiceMessage(testStarted(testName, "file://$filePath"))
    }

    override fun emitTestPassed(testName: String, messages: String, duration: String) {
        if (messages.isNotEmpty()) {
            emitServiceMessage(testStdOut(testName, messages))
        }

        val calculatedDuration = if (duration.isNotEmpty()) {
            convertDurationToMs(duration)
        } else {
            val startTime = testStartTimes[testName]
            if (startTime != null) {
                (System.currentTimeMillis() - startTime).toString()
            } else "0"
        }

        testStartTimes.remove(testName)
        emitServiceMessage(testFinished(testName, calculatedDuration))
    }

    override fun emitTestFailed(testName: String, messages: String, duration: String) {
        if (messages.isNotEmpty()) {
            emitServiceMessage(testStdOut(testName,messages))
            emitServiceMessage(testFailed(testName))
        }

        val calculatedDuration = if (duration.isNotEmpty()) {
            convertDurationToMs(duration)
        } else {
            val startTime = testStartTimes[testName]
            if (startTime != null) {
                (System.currentTimeMillis() - startTime).toString()
            } else "0"
        }

        testStartTimes.remove(testName)
        emitServiceMessage(testFinished(testName, calculatedDuration))
    }

    internal fun convertDurationToMs(duration: String): String {
        return when {
            duration.endsWith("ms") -> duration.dropLast(2)
            duration.endsWith("s") -> {
                val seconds = duration.dropLast(1).toDoubleOrNull() ?: 0.0
                (seconds * 1000).toInt().toString()
            }
            duration.endsWith("m") -> {
                val minutes = duration.dropLast(1).toDoubleOrNull() ?: 0.0
                (minutes * 60 * 1000).toInt().toString()
            }
            else -> {
                // Try to parse as plain number (assume milliseconds)
                duration.toIntOrNull()?.toString() ?: "0"
            }
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
}
