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

        // Add a listener to handle the process exit code
        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                if (event.exitCode != 0) {
                    eventEmitter.emitBuildProblem(
                        "TestCafe process exited with non-zero exit code: ${event.exitCode}",
                        "testcafe-exit-code-${event.exitCode}"
                    )
                }
            }
        })

        return processHandler
    }
}

