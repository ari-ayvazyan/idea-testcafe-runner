package at.itdo.tcrunner.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class RunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RunConfigurationOptions>(project, factory, name) {

    override fun getOptions(): RunConfigurationOptions {
        return super.getOptions() as RunConfigurationOptions
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return RunConfigurationEditor()
    }

    override fun checkConfiguration() {
        super.checkConfiguration()

        val scriptPath = options.getScriptPath()
        if (scriptPath.isBlank() && options.getCustomCommand().isBlank()) {
            throw RuntimeConfigurationError("Either script path or custom command must be specified")
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RunProfileState(environment, this)
    }

    // Getters for configuration options
    fun getScriptPath(): String = options.getScriptPath()
    fun setScriptPath(value: String) = options.setScriptPath(value)

    fun getTestFilter(): String = options.getTestFilter()
    fun setTestFilter(value: String) = options.setTestFilter(value)

    fun getFixtureFilter(): String = options.getFixtureFilter()
    fun setFixtureFilter(value: String) = options.setFixtureFilter(value)

    fun getBrowser(): String = options.getBrowser()
    fun setBrowser(value: String) = options.setBrowser(value)

    fun getHeadlessMode(): Boolean = options.getHeadlessMode()
    fun setHeadlessMode(value: Boolean) = options.setHeadlessMode(value)

    fun getLiveMode(): Boolean = options.getLiveMode()
    fun setLiveMode(value: Boolean) = options.setLiveMode(value)

    fun getCustomCommand(): String = options.getCustomCommand()
    fun setCustomCommand(value: String) = options.setCustomCommand(value)

    fun getWorkingDirectory(): String = options.getWorkingDirectory()
    fun setWorkingDirectory(value: String) = options.setWorkingDirectory(value)

    fun getEnvs(): MutableMap<String, String> = options.envs
    fun setEnvs(envs: MutableMap<String, String>) {
        options.envs = envs
    }
}
