package at.itdo.tcrunner.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons
import javax.swing.Icon

class TestCafeConfigurationType : ConfigurationType {

    companion object {
        const val ID = "TestCafeRunConfiguration"
        val INSTANCE = TestCafeConfigurationType()
    }

    override fun getDisplayName(): String = "TestCafe"

    override fun getConfigurationTypeDescription(): String = "TestCafe test runner configuration"

    override fun getIcon(): Icon = AllIcons.RunConfigurations.TestState.Run

    override fun getId(): String = ID

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(TestCafeConfigurationFactory(this))
    }
}
