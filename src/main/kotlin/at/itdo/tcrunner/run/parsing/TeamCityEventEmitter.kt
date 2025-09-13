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

    fun setCurrentProcessEvent(event: ProcessEvent) {
        currentProcessEvent = event
    }

    fun isProcessingServiceMessage() = isProcessingServiceMessage

    override fun emitTestRunStarted() {
        emitServiceMessage(testRunStarted())
        emitServiceMessage(testSuiteStarted("TestCafe Tests"))
    }

    override fun emitTestRunFinished() {
        emitServiceMessage(testSuiteFinished("TestCafe Tests"))
    }

    override fun emitFixtureStarted(fixtureName: String, filePath: String) {
        emitServiceMessage(testSuiteStarted(fixtureName, "file://$filePath"))
    }

    override fun emitFixtureFinished(fixtureName: String) {
        emitServiceMessage(testSuiteFinished(fixtureName))
    }

    override fun emitTestStarted(testName: String, filePath: String) {
        emitServiceMessage(testStarted(testName, "file://$filePath"))
    }

    override fun emitTestPassed(testName: String, messages: String) {
        if (messages.isNotEmpty()) {
            emitServiceMessage(testStdOut(testName, messages))
        }
        emitServiceMessage(testFinished(testName))
    }

    override fun emitTestFailed(testName: String, messages: String) {
        if (messages.isNotEmpty()) {
            emitServiceMessage(testFailed(testName, messages))
        }
        emitServiceMessage(testFinished(testName))
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
