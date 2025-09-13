package at.itdo.tcrunner.run.parsing

import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.util.Key

/**
 * Handles process output and delegates to TestCafe parser
 */
class ProcessOutputHandler(
    private val testFilePath: String,
    private val eventEmitter: TeamCityEventEmitter
) : ProcessAdapter() {

    private lateinit var parser: TestCafeOutputParser

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (outputType == ProcessOutputTypes.STDOUT && !eventEmitter.isProcessingServiceMessage()) {
            initializeParserIfNeeded(event)

            val text = event.text
            if (isServiceMessage(text)) return

            processTextLines(text)
        }
    }

    override fun processTerminated(event: ProcessEvent) {
        if (::parser.isInitialized) {
            parser.finalizeParsing()
        }
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
