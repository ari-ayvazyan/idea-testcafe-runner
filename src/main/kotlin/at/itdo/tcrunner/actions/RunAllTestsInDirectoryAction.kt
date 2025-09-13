package at.itdo.tcrunner.actions

import at.itdo.tcrunner.FileDetector
import at.itdo.tcrunner.run.RunConfigurationUtils
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class RunAllTestsInDirectoryAction : TestBaseAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        selectedFiles.forEach { selectedFile ->
            val testFiles = getTestCafeFiles(project, selectedFile)
            if (testFiles.isNotEmpty()) {
                testFiles.forEach { file ->
                    // Execute entire file using temporary run configuration
                    RunConfigurationUtils.executeFileWithTemporaryRunConfiguration(
                        project,
                        file.path,
                        "TestCafe Directory: ${file.nameWithoutExtension}"
                    )
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        e.presentation.isEnabledAndVisible = project != null &&
            selectedFiles != null &&
            selectedFiles.isNotEmpty() &&
            selectedFiles.any { it.isDirectory || FileDetector(project).isTestCafeFile(it) }
    }
}
