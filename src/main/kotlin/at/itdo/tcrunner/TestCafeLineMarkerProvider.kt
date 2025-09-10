package at.itdo.tcrunner

import at.itdo.tcrunner.run.TestCafeRunConfigurationUtils
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class TestCafeLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Only process leaf elements to avoid performance warnings
        if (element.firstChild != null) {
            return null
        }

        val psiFile = element.containingFile ?: return null
        val project = element.project

        if (!TestCafeFileDetector(project).isTestCafeFile(psiFile)) {
            return null
        }

        // Check if this element is specifically the "fixture" or "test" identifier
        if (!isDeclarationIdentifier(element)) {
            return null
        }

        val analyzer = TestCafeASTAnalyzer()
        val declarations = analyzer.findTestCafeDeclarations(psiFile)

        // Find the declaration that contains this element
        val elementOffset = element.textOffset
        val matchingDeclaration = declarations.find { declaration ->
            elementOffset >= declaration.startOffset && elementOffset < declaration.endOffset
        } ?: return null

        return createLineMarkerInfo(element, matchingDeclaration, psiFile, project)
    }

    private fun isDeclarationIdentifier(element: PsiElement): Boolean {
        val text = element.text
        return text == "fixture" || text == "test"
    }

    private fun createLineMarkerInfo(
        element: PsiElement,
        declaration: TestCafeDeclaration,
        psiFile: PsiFile,
        project: Project
    ): LineMarkerInfo<PsiElement> {

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.RunConfigurations.TestState.Run,
            { getTooltipText(declaration) },
            { _, _ ->
                // Create and execute temporary run configuration
                val filePath = psiFile.virtualFile?.path ?: return@LineMarkerInfo
                TestCafeRunConfigurationUtils.executeWithTemporaryRunConfiguration(project, declaration, filePath)
            },
            GutterIconRenderer.Alignment.CENTER,
            { getTooltipText(declaration) }
        )
    }

    private fun getTooltipText(declaration: TestCafeDeclaration): String {
        return when (declaration) {
            is TestCafeDeclaration.Test -> "Run TestCafe test: '${declaration.name}'"
            is TestCafeDeclaration.Fixture -> "Run TestCafe fixture: '${declaration.name}'"
        }
    }
}
