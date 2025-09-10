package at.itdo.tcrunner.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class TestCafeConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    companion object {
        const val FACTORY_NAME = "TestCafe"
    }

    override fun getId(): String = FACTORY_NAME

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return TestCafeRunConfiguration(project, this, "TestCafe")
    }

    override fun getName(): String = FACTORY_NAME

    override fun getOptionsClass(): Class<out BaseState>? = TestCafeRunConfigurationOptions::class.java
}
