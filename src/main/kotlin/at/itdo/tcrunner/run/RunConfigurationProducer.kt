package at.itdo.tcrunner.run

import at.itdo.tcrunner.Declaration
import at.itdo.tcrunner.ASTAnalyzer
import at.itdo.tcrunner.FileDetector
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class RunConfigurationProducer : LazyRunConfigurationProducer<RunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return ConfigurationType.INSTANCE.configurationFactories[0]
    }

    override fun isConfigurationFromContext(
        configuration: RunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val element = context.psiLocation ?: return false
        val virtualFile = element.containingFile?.virtualFile ?: return false

        return configuration.getScriptPath() == virtualFile.path
    }

    override fun setupConfigurationFromContext(
        configuration: RunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val element = context.psiLocation ?: return false
        val containingFile = element.containingFile ?: return false
        val virtualFile = containingFile.virtualFile ?: return false

        // Check if this is a TestCafe file using the centralized detector
        val detector = FileDetector(context.project)
        if (!detector.isTestCafeFile(virtualFile)) {
            return false
        }

        // Parse file to find TestCafe declarations
        val analyzer = ASTAnalyzer()
        val declarations = analyzer.findTestCafeDeclarations(containingFile)

        // Find the declaration at current position
        val declaration = findDeclarationAtPosition(declarations, element)

        // Check if there are exclusive declarations in the file
        val hasExclusiveDeclarations = analyzer.hasExclusiveDeclarations(containingFile)

        // If there are exclusive declarations, only allow configuration for exclusive ones
        if (hasExclusiveDeclarations && declaration != null && !declaration.isExclusive) {
            return false
        }

        configuration.setScriptPath(virtualFile.path)

        when (declaration) {
            is Declaration.Test -> {
                configuration.setTestFilter(declaration.name)
                val prefix = if (declaration.isExclusive) "TestCafe (exclusive):" else "TestCafe:"
                configuration.name = "$prefix ${declaration.name}"
            }
            is Declaration.Fixture -> {
                configuration.setFixtureFilter(declaration.name)
                val prefix = if (declaration.isExclusive) "TestCafe (exclusive):" else "TestCafe:"
                configuration.name = "$prefix ${declaration.name}"
            }
            else -> {
                // If there are exclusive declarations in the file but we didn't find a specific one,
                // don't create a generic configuration
                if (hasExclusiveDeclarations) {
                    return false
                }
                configuration.name = "TestCafe: ${virtualFile.nameWithoutExtension}"
            }
        }

        return true
    }


    private fun findDeclarationAtPosition(
        declarations: List<Declaration>,
        element: PsiElement
    ): Declaration? {
        val offset = element.textOffset
        return declarations.find { declaration ->
            offset >= declaration.startOffset && offset <= declaration.endOffset
        }
    }
}
