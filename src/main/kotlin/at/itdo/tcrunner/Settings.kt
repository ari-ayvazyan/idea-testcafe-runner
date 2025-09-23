package at.itdo.tcrunner

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(
    name = "TestCafeSettings",
    storages = [Storage(".idea/testcafe-runner.xml")]
)
class Settings : PersistentStateComponent<Settings> {

    var defaultCommand: String = "npx testcafe {browser} {filePath}"
    var testCommand: String = "npx testcafe {browser} {filePath} -t \"{testName}\""
    var fixtureCommand: String = "npx testcafe {browser} {filePath} -f \"{fixtureName}\""
    var headlessMode: Boolean = false
    var liveMode: Boolean = false
    var browser: String = "chrome"
    var concurrency: Int = 1
    var timeout: Int = 30000
    var filePatterns: String = FileDetector.DEFAULT_PATTERNS.joinToString(",")
    var workingDirectory: String = ""

    companion object {
        fun getInstance(project: Project): Settings {
            return project.service<Settings>()
        }
    }

    override fun getState(): Settings = this

    override fun loadState(state: Settings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun getCommandTemplate(type: CommandType): String {
        return when (type) {
            CommandType.FILE -> defaultCommand
            CommandType.TEST -> testCommand
            CommandType.FIXTURE -> fixtureCommand
        }
    }

    fun setCommandTemplate(type: CommandType, command: String) {
        when (type) {
            CommandType.FILE -> defaultCommand = command
            CommandType.TEST -> testCommand = command
            CommandType.FIXTURE -> fixtureCommand = command
        }
    }

    enum class CommandType {
        FILE, TEST, FIXTURE
    }

    fun getFilePatternsAsSet(): Set<String> {
        return filePatterns.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }
}
