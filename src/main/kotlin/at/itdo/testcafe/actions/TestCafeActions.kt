package at.itdo.testcafe.actions

import at.itdo.testcafe.TestCafeDeclaration
import at.itdo.testcafe.TestCafeFileDetector
import at.itdo.testcafe.TestCafeASTAnalyzer
import at.itdo.testcafe.run.TestCafeRunConfigurationUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

abstract class TestCafeBaseAction : AnAction() {
    
    protected fun getTestCafeFiles(project: Project, directory: VirtualFile): List<VirtualFile> {
        val detector = TestCafeFileDetector(project)
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

class RunAllTestsInProjectAction : TestCafeBaseAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectDir = project.baseDir ?: return
        
        val testFiles = getTestCafeFiles(project, projectDir)
        if (testFiles.isEmpty()) {
            return
        }
        
        testFiles.forEach { file ->
            // Execute entire file using temporary run configuration
            TestCafeRunConfigurationUtils.executeFileWithTemporaryRunConfiguration(
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

class RunAllTestsInDirectoryAction : TestCafeBaseAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return
        
        selectedFiles.forEach { selectedFile ->
            val testFiles = getTestCafeFiles(project, selectedFile)
            if (testFiles.isNotEmpty()) {
                testFiles.forEach { file ->
                    // Execute entire file using temporary run configuration
                    TestCafeRunConfigurationUtils.executeFileWithTemporaryRunConfiguration(
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
            selectedFiles.any { it.isDirectory || TestCafeFileDetector(project).isTestCafeFile(it) }
    }
}