package at.itdo.tcrunner

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

class FileDetector(private val project: Project) {

    private val settings = Settings.getInstance(project)

    companion object {
        // Default fallback regex patterns if settings are not available
        private val DEFAULT_PATTERNS = setOf(
            ".*\\.spec\\.(js|ts)$",
            ".*\\.tests?\\.(js|ts)$",
            ".*-tests?\\.(js|ts)$"
        )
    }

    fun isTestCafeFile(virtualFile: VirtualFile): Boolean {
        val patterns = settings.getFilePatternsAsSet().takeIf { it.isNotEmpty() } ?: DEFAULT_PATTERNS

        return patterns.any { pattern ->
            Regex(pattern, RegexOption.IGNORE_CASE).matches(virtualFile.name)
        }
    }

    fun isTestCafeFile(psiFile: PsiFile): Boolean {
        return psiFile.virtualFile?.let { isTestCafeFile(it) } ?: false
    }

    fun hasTestCafeContent(psiFile: PsiFile): Boolean {
        val text = psiFile.text
        return text.contains("fixture(") || text.contains("test(")
    }
}
