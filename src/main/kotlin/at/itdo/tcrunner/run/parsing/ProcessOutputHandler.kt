package at.itdo.tcrunner.run.parsing

import at.itdo.tcrunner.util.TCLogger
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

/**
 * Handles process output and delegates to TestCafe parser
 */
class ProcessOutputHandler(
    private val project: Project,
    private val testFilePath: String,
    private val eventEmitter: TeamCityEventEmitter
) : ProcessListener {

    private lateinit var parser: TestCafeOutputParser

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        val text = event.text
        TCLogger.debug(project, "Raw output ($outputType): $text")

        if (outputType == ProcessOutputTypes.STDOUT && !eventEmitter.isProcessingServiceMessage()) {
            initializeParserIfNeeded(event)

            if (isServiceMessage(text)) return

            processTextLines(text)
        }
    }

    override fun startNotified(event: ProcessEvent) {
        // No action needed on process start
    }

    override fun processTerminated(event: ProcessEvent) {
        if (::parser.isInitialized) {
            parser.finalizeParsing()
        }
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
        // No action needed before process termination
    }

    private fun initializeParserIfNeeded(event: ProcessEvent) {
        if (!::parser.isInitialized) {
            eventEmitter.setCurrentProcessEvent(event)
            parser = TestCafeOutputParser(testFilePath, eventEmitter)
        }
    }

    private fun isServiceMessage(text: String) = text.startsWith("##teamcity[")

    private fun processTextLines(text: String) {
        text.lines().forEach { line ->
            parser.processLine(line)
        }
    }
}
