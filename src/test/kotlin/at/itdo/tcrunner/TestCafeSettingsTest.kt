package at.itdo.tcrunner

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestCafeSettingsTest {

    private lateinit var settings: TestCafeSettings

    @BeforeEach
    fun setUp() {
        settings = TestCafeSettings()
    }

    @Test
    fun testDefaultValues() {
        assertEquals("npx testcafe {browser} {filePath}", settings.defaultCommand)
        assertEquals("npx testcafe {browser} {filePath} -t \"{testName}\"", settings.testCommand)
        assertEquals("npx testcafe {browser} {filePath} -f \"{fixtureName}\"", settings.fixtureCommand)
        assertEquals(false, settings.headlessMode)
        assertEquals(false, settings.liveMode)
        assertEquals("chrome", settings.browser)
        assertEquals(1, settings.concurrency)
        assertEquals(30000, settings.timeout)
    }

    @Test
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

    @Test
    fun testSetCommandTemplate() {
        val newFileCommand = "yarn testcafe firefox {filePath}"
        settings.setCommandTemplate(TestCafeSettings.CommandType.FILE, newFileCommand)

        assertEquals(newFileCommand, settings.defaultCommand)
        assertEquals(newFileCommand, settings.getCommandTemplate(TestCafeSettings.CommandType.FILE))
    }

    @Test
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