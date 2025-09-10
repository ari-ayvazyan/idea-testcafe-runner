package at.itdo.tcrunner

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

class TestCafeFileDetector(private val project: Project) {

    private val settings = TestCafeSettings.getInstance(project)

    companion object {
        // Default fallback patterns if settings are not available
        private val DEFAULT_PATTERNS = setOf(
            "*.spec.js", "*.spec.ts", "*.test.js", "*.test.ts", "*-test.js", "*-test.ts"
        )
    }

    private fun convertGlobToRegex(glob: String): Regex {
        val escaped = glob
            .replace(".", "\\.")
            .replace("*", ".*")
        return Regex("^$escaped$", RegexOption.IGNORE_CASE)
    }

    fun isTestCafeFile(virtualFile: VirtualFile): Boolean {
        val patterns = settings.getFilePatternsAsSet().takeIf { it.isNotEmpty() } ?: DEFAULT_PATTERNS

        return patterns.any { pattern ->
            convertGlobToRegex(pattern).matches(virtualFile.name)
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
