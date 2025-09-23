package at.itdo.tcrunner.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty
import com.intellij.util.xmlb.annotations.OptionTag

class RunConfigurationOptions : RunConfigurationOptions() {

    private val scriptPath: StoredProperty<String?> = string("").provideDelegate(this, "scriptPath")
    private val testFilter: StoredProperty<String?> = string("").provideDelegate(this, "testFilter")
    private val fixtureFilter: StoredProperty<String?> = string("").provideDelegate(this, "fixtureFilter")
    private val browser: StoredProperty<String?> = string("chrome").provideDelegate(this, "browser")
    private val headlessMode: StoredProperty<Boolean> = property(false).provideDelegate(this, "headlessMode")
    private val liveMode: StoredProperty<Boolean> = property(false).provideDelegate(this, "liveMode")
    private val customCommand: StoredProperty<String?> = string("").provideDelegate(this, "customCommand")
    private val workingDirectory: StoredProperty<String?> = string("").provideDelegate(this, "workingDirectory")
    @get:OptionTag("envs")
    var envs: MutableMap<String, String> by linkedMap()


    fun getScriptPath(): String = scriptPath.getValue(this) ?: ""
    fun setScriptPath(value: String) { scriptPath.setValue(this, value) }

    fun getTestFilter(): String = testFilter.getValue(this) ?: ""
    fun setTestFilter(value: String) { testFilter.setValue(this, value) }

    fun getFixtureFilter(): String = fixtureFilter.getValue(this) ?: ""
    fun setFixtureFilter(value: String) { fixtureFilter.setValue(this, value) }

    fun getBrowser(): String = browser.getValue(this) ?: "chrome"
    fun setBrowser(value: String) { browser.setValue(this, value) }

    fun getHeadlessMode(): Boolean = headlessMode.getValue(this)
    fun setHeadlessMode(value: Boolean) { headlessMode.setValue(this, value) }

    fun getLiveMode(): Boolean = liveMode.getValue(this)
    fun setLiveMode(value: Boolean) { liveMode.setValue(this, value) }

    fun getCustomCommand(): String = customCommand.getValue(this) ?: ""
    fun setCustomCommand(value: String) { customCommand.setValue(this, value) }

    fun getWorkingDirectory(): String = workingDirectory.getValue(this) ?: ""
    fun setWorkingDirectory(value: String) { workingDirectory.setValue(this, value) }
}
