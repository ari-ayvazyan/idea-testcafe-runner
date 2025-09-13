package at.itdo.tcrunner.run.parsing

import com.intellij.execution.process.*

/**
 * Factory for creating TestCafe process handlers with SM Test Runner integration
 */
object TestCafeProcessWrapper {

    fun createProcessHandler(
        process: Process,
        commandLine: String,
        testFilePath: String
    ): ProcessHandler {
        val processHandler = KillableColoredProcessHandler(process, commandLine)
        val eventEmitter = TeamCityEventEmitter()
        val outputHandler = ProcessOutputHandler(testFilePath, eventEmitter)

        processHandler.addProcessListener(outputHandler)
        return processHandler
    }
}

