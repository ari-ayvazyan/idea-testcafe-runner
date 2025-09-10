package at.itdo.tcrunner.run

import at.itdo.tcrunner.TestCafeDeclaration
import at.itdo.tcrunner.TestCafeASTAnalyzer
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class TestCafeRunConfigurationProducer : LazyRunConfigurationProducer<TestCafeRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return TestCafeConfigurationType.INSTANCE.configurationFactories[0]
    }

    override fun isConfigurationFromContext(
        configuration: TestCafeRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val element = context.psiLocation ?: return false
        val virtualFile = element.containingFile?.virtualFile ?: return false

        return configuration.getScriptPath() == virtualFile.path
    }

    override fun setupConfigurationFromContext(
        configuration: TestCafeRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val element = context.psiLocation ?: return false
        val containingFile = element.containingFile ?: return false
        val virtualFile = containingFile.virtualFile ?: return false

        // Check if this is a TestCafe file
        if (!isTestCafeFile(virtualFile.name)) {
            return false
        }

        // Parse file to find TestCafe declarations
        val analyzer = TestCafeASTAnalyzer()
        val declarations = analyzer.findTestCafeDeclarations(containingFile)

        // Find the declaration at current position
        val declaration = findDeclarationAtPosition(declarations, element)

        configuration.setScriptPath(virtualFile.path)

        when (declaration) {
            is TestCafeDeclaration.Test -> {
                configuration.setTestFilter(declaration.name)
                configuration.name = "TestCafe: ${declaration.name}"
            }
            is TestCafeDeclaration.Fixture -> {
                configuration.setFixtureFilter(declaration.name)
                configuration.name = "TestCafe: ${declaration.name}"
            }
            else -> {
                configuration.name = "TestCafe: ${virtualFile.nameWithoutExtension}"
            }
        }

        return true
    }

    private fun isTestCafeFile(fileName: String): Boolean {
        val patterns = listOf(".spec.js", ".spec.ts", ".test.js", ".test.ts", "-test.js", "-test.ts")
        return patterns.any { fileName.endsWith(it) }
    }

    private fun findDeclarationAtPosition(
        declarations: List<TestCafeDeclaration>,
        element: PsiElement
    ): TestCafeDeclaration? {
        val offset = element.textOffset
        return declarations.find { declaration ->
            offset >= declaration.startOffset && offset <= declaration.endOffset
        }
    }
}
