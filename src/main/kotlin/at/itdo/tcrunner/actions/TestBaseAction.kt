package at.itdo.tcrunner.actions

import at.itdo.tcrunner.FileDetector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

abstract class TestBaseAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    protected fun getTestCafeFiles(project: Project, directory: VirtualFile): List<VirtualFile> {
        val detector = FileDetector(project)
        val testFiles = mutableListOf<VirtualFile>()

        fun collectTestFiles(dir: VirtualFile) {
            dir.children?.forEach { child ->
                if (child.isDirectory) {
                    collectTestFiles(child)
                } else if (detector.isTestCafeFile(child)) {
                    testFiles.add(child)
                }
            }
        }

        if (directory.isDirectory) {
            collectTestFiles(directory)
        } else if (detector.isTestCafeFile(directory)) {
            testFiles.add(directory)
        }

        return testFiles
    }
}

