package at.itdo.testcafe

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TestCafeSettingsTest : BasePlatformTestCase() {
    
    private lateinit var settings: TestCafeSettings
    
    override fun setUp() {
        super.setUp()
        settings = TestCafeSettings.getInstance(project)
    }
    
    fun testDefaultValues() {
        assertEquals("npx testcafe chrome {filePath}", settings.defaultCommand)
        assertEquals("npx testcafe chrome {filePath} -t \"{testName}\"", settings.testCommand)
        assertEquals("npx testcafe chrome {filePath} -f \"{fixtureName}\"", settings.fixtureCommand)
        assertEquals(false, settings.headlessMode)
        assertEquals(false, settings.liveMode)
        assertEquals("chrome", settings.browser)
        assertEquals(1, settings.concurrency)
        assertEquals(30000, settings.timeout)
    }
    
    fun testGetCommandTemplate() {
        assertEquals(
            settings.defaultCommand,
            settings.getCommandTemplate(TestCafeSettings.CommandType.FILE)
        )
        assertEquals(
            settings.testCommand,
            settings.getCommandTemplate(TestCafeSettings.CommandType.TEST)
        )
        assertEquals(
            settings.fixtureCommand,
            settings.getCommandTemplate(TestCafeSettings.CommandType.FIXTURE)
        )
    }
    
    fun testSetCommandTemplate() {
        val newFileCommand = "yarn testcafe firefox {filePath}"
        settings.setCommandTemplate(TestCafeSettings.CommandType.FILE, newFileCommand)
        
        assertEquals(newFileCommand, settings.defaultCommand)
        assertEquals(newFileCommand, settings.getCommandTemplate(TestCafeSettings.CommandType.FILE))
    }
    
    fun testStateManagement() {
        settings.browser = "firefox"
        settings.headlessMode = true
        settings.concurrency = 4
        
        val state = settings.state
        assertNotNull(state)
        assertEquals("firefox", state.browser)
        assertEquals(true, state.headlessMode)
        assertEquals(4, state.concurrency)
        
        val newSettings = TestCafeSettings()
        newSettings.loadState(state)
        
        assertEquals("firefox", newSettings.browser)
        assertEquals(true, newSettings.headlessMode)
        assertEquals(4, newSettings.concurrency)
    }
}