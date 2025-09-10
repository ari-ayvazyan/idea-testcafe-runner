package at.itdo.tcrunner

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestCafeCommandExecutorTest {

    private lateinit var settings: Settings

    @BeforeEach
    fun setUp() {
        settings = Settings()
    }

    @Test
    fun testBuildTestCommand() {
        val testDeclaration = MockTestCafeDeclaration.Test(
            name = "Login test",
            startOffset = 0,
            endOffset = 10
        )

        val filePath = "/path/to/test.spec.js"

        val result = buildTestCommandForTest(settings, testDeclaration, filePath)

        assertEquals("npx testcafe chrome /path/to/test.spec.js -t \"Login test\"", result)
    }

    @Test
    fun testBuildFixtureCommand() {
        val fixtureDeclaration = MockTestCafeDeclaration.Fixture(
            name = "Login Page",
            startOffset = 0,
            endOffset = 10,
            page = "https://example.com"
        )

        val filePath = "/path/to/test.spec.js"

        val result = buildFixtureCommandForTest(settings, fixtureDeclaration, filePath)

        assertEquals("npx testcafe chrome /path/to/test.spec.js -f \"Login Page\"", result)
    }

    @Test
    fun testBuildFileCommand() {
        val filePath = "/path/to/test.spec.js"

        val result = buildFileCommandForTest(settings, filePath)

        assertEquals("npx testcafe chrome /path/to/test.spec.js", result)
    }
}

// Helper functions that replicate the private methods for testing
fun buildTestCommandForTest(settings: Settings, test: MockTestCafeDeclaration.Test, filePath: String): String {
    return settings.getCommandTemplate(Settings.CommandType.TEST)
        .replace("{filePath}", filePath)
        .replace("{testName}", test.name)
        .replace("{browser}", settings.browser)
}

fun buildFixtureCommandForTest(settings: Settings, fixture: MockTestCafeDeclaration.Fixture, filePath: String): String {
    return settings.getCommandTemplate(Settings.CommandType.FIXTURE)
        .replace("{filePath}", filePath)
        .replace("{fixtureName}", fixture.name)
        .replace("{browser}", settings.browser)
}

fun buildFileCommandForTest(settings: Settings, filePath: String): String {
    return settings.getCommandTemplate(Settings.CommandType.FILE)
        .replace("{filePath}", filePath)
        .replace("{browser}", settings.browser)
}
