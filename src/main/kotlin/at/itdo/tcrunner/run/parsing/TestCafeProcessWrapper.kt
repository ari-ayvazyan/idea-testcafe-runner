package at.itdo.tcrunner.run.parsing

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.project.Project

/**
 * Factory for creating TestCafe process handlers with SM Test Runner integration
 */
object TestCafeProcessWrapper {

    fun createProcessHandler(
        project: Project,
        commandLine: GeneralCommandLine,
        testFilePath: String
    ): ProcessHandler {
        val processHandler = KillableColoredProcessHandler(commandLine)
        val eventEmitter = TeamCityEventEmitter()
        val outputHandler = ProcessOutputHandler(project, testFilePath, eventEmitter)

        processHandler.addProcessListener(outputHandler)
        return processHandler
    }
}
