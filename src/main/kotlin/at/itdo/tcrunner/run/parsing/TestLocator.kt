package at.itdo.tcrunner.run.parsing

import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

class TestLocator : SMTestLocator {

    companion object {
        const val PROTOCOL = "file"
    }

    override fun getLocation(
        protocol: String,
        path: String,
        project: Project,
        scope: GlobalSearchScope
    ): List<Location<*>> {
        if (protocol != PROTOCOL) {
            return emptyList()
        }

        val locations = mutableListOf<Location<*>>()

        try {
            // Parse path - it should be a file path
            val virtualFile = VfsUtil.findFileByIoFile(File(path), true)
            if (virtualFile != null) {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile != null) {
                    locations.add(PsiLocation(project, psiFile))
                }
            }
        } catch (e: Exception) {
            // Ignore invalid paths
        }

        return locations
    }
}
