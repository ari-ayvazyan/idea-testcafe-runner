package at.itdo.testcafe

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(
    name = "TestCafeSettings",
    storages = [Storage("testcafe-runner.xml")]
)
class TestCafeSettings : PersistentStateComponent<TestCafeSettings> {
    
    var defaultCommand: String = "npx testcafe chrome {filePath}"
    var testCommand: String = "npx testcafe chrome {filePath} -t \"{testName}\""
    var fixtureCommand: String = "npx testcafe chrome {filePath} -f \"{fixtureName}\""
    var headlessMode: Boolean = false
    var liveMode: Boolean = false
    var browser: String = "chrome"
    var concurrency: Int = 1
    var timeout: Int = 30000
    var filePatterns: String = "*.spec.js,*.spec.ts,*.test.js,*.test.ts,*-test.js,*-test.ts"
    
    companion object {
        fun getInstance(project: Project): TestCafeSettings {
            return project.service<TestCafeSettings>()
        }
    }
    
    override fun getState(): TestCafeSettings = this
    
    override fun loadState(state: TestCafeSettings) {
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