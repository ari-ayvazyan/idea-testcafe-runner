package at.itdo.tcrunner.actions

import at.itdo.tcrunner.run.RunConfigurationUtils
import com.intellij.openapi.actionSystem.AnActionEvent

class RunAllTestsInProjectAction : TestBaseAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectDir = project.projectFile?.parent ?: return

        val testFiles = getTestCafeFiles(project, projectDir)
        if (testFiles.isEmpty()) {
            return
        }

        testFiles.forEach { file ->
            // Execute entire file using temporary run configuration
            RunConfigurationUtils.executeFileWithTemporaryRunConfiguration(
                project,
                file.path,
                "TestCafe All Tests: ${file.nameWithoutExtension}"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}
