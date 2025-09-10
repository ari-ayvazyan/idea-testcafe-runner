package at.itdo.tcrunner

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.icons.AllIcons
import java.io.File

class TestCafeCommandExecutor(private val project: Project) {

    fun executeTest(declaration: TestCafeDeclaration, filePath: String) {
        val command = when (declaration) {
            is TestCafeDeclaration.Test -> buildTestCommand(declaration, filePath)
            is TestCafeDeclaration.Fixture -> buildFixtureCommand(declaration, filePath)
        }

        executeCommand(command, File(filePath).parentFile)
    }

    fun executeFile(filePath: String) {
        val command = buildFileCommand(filePath)
        executeCommand(command, File(filePath).parentFile)
    }

    private fun buildTestCommand(test: TestCafeDeclaration.Test, filePath: String): String {
        val settings = TestCafeSettings.getInstance(project)
        return settings.getCommandTemplate(TestCafeSettings.CommandType.TEST)
            .replace("{filePath}", filePath)
            .replace("{testName}", test.name)
            .replace("{browser}", settings.browser)
    }

    private fun buildFixtureCommand(fixture: TestCafeDeclaration.Fixture, filePath: String): String {
        val settings = TestCafeSettings.getInstance(project)
        return settings.getCommandTemplate(TestCafeSettings.CommandType.FIXTURE)
            .replace("{filePath}", filePath)
            .replace("{fixtureName}", fixture.name)
            .replace("{browser}", settings.browser)
    }

    private fun buildFileCommand(filePath: String): String {
        val settings = TestCafeSettings.getInstance(project)
        return settings.getCommandTemplate(TestCafeSettings.CommandType.FILE)
            .replace("{filePath}", filePath)
            .replace("{browser}", settings.browser)
    }

    private fun executeCommand(command: String, workingDirectory: File?) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val commandLine = GeneralCommandLine()
                    .withWorkDirectory(workingDirectory)
                    .withExePath("bash")
                    .withParameters("-c", command)

                val processHandler = ProcessHandlerFactory.getInstance()
                    .createProcessHandler(commandLine)

                ApplicationManager.getApplication().invokeLater {
                    showInRunToolWindow(processHandler, command)
                }

            } catch (e: ExecutionException) {
                ApplicationManager.getApplication().invokeLater {
                    showError("Failed to execute TestCafe command: ${e.message}")
                }
            }
        }
    }

    private fun showInRunToolWindow(processHandler: ProcessHandler, command: String) {
        val console = ConsoleViewImpl(project, true)

        val descriptor = RunContentDescriptor(
            console,
            processHandler,
            console.component,
            "TestCafe: $command",
            AllIcons.RunConfigurations.TestState.Run
        )

        console.attachToProcess(processHandler)

        val runContentManager = RunContentManager.getInstance(project)
        runContentManager.showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)

        processHandler.startNotify()
    }

    private fun showError(message: String) {
        val console = ConsoleViewImpl(project, true)
        console.print("Error: $message\n", ConsoleViewContentType.ERROR_OUTPUT)

        val descriptor = RunContentDescriptor(
            console,
            null,
            console.component,
            "TestCafe Error",
            AllIcons.General.Error
        )

        val runContentManager = RunContentManager.getInstance(project)
        runContentManager.showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)
    }
}
