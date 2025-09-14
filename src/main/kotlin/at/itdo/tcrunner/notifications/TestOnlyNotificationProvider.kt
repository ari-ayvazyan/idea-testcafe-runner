package at.itdo.tcrunner.notifications

import at.itdo.tcrunner.ASTAnalyzer
import at.itdo.tcrunner.FileDetector
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

class TestOnlyNotificationProvider : EditorNotificationProvider {

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        val fileDetector = FileDetector(project)

        // Only process TestCafe files
        if (!fileDetector.isTestCafeFile(file)) {
            return null
        }

        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
        val astAnalyzer = ASTAnalyzer()

        if (!astAnalyzer.hasOnlyUsage(psiFile)) {
            return null
        }

        return Function { _ ->
            createNotificationPanel(astAnalyzer.getOnlyUsageDetails(psiFile))
        }
    }

    private fun createNotificationPanel(onlyUsageInfo: ASTAnalyzer.OnlyUsageInfo): EditorNotificationPanel {
        val panel = EditorNotificationPanel()

        // Set the main message
        panel.text = onlyUsageInfo.getDisplayMessage()

        return panel
    }
}
