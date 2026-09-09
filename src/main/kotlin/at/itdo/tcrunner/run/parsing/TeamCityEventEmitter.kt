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
            emitServiceMessage(testStdOut(testName, messages))
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
        val trimmed = duration.trim()
        if (trimmed.isEmpty()) return "0"

        var totalMs = 0.0
        // Match numbers followed by m, s, or ms (e.g. "1m 30.5s", "1.5s", "500ms")
        val pattern = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*(ms|s|m)")
        val matches = pattern.findAll(trimmed).toList()

        if (matches.isNotEmpty()) {
            for (match in matches) {
                val value = match.groupValues[1].toDoubleOrNull() ?: 0.0
                val unit = match.groupValues[2]
                when (unit) {
                    "ms" -> totalMs += value
                    "s" -> totalMs += value * 1000
                    "m" -> totalMs += value * 60 * 1000
                }
            }
            return totalMs.toInt().toString()
        }

        // Fallback: try parsing as a raw number
        return trimmed.toIntOrNull()?.toString() ?: "0"
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
