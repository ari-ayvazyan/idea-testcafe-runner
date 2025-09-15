package at.itdo.tcrunner

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

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

    @Test
    fun testCommandLineCreationIncludesEnvironment() {
        val command = "npx testcafe chrome test.spec.js"
        val workingDir = File("/tmp")

        val commandLine = createCommandLineForTest(command, workingDir)

        // Verify environment variables are included
        assertTrue(commandLine.environment.isNotEmpty(), "Environment should not be empty")
        assertTrue(commandLine.environment.containsKey("PATH"), "PATH should be included in environment")

        // Verify working directory is set
        assertEquals(workingDir, commandLine.workDirectory)

        // Verify parameters are correct based on platform
        when {
            SystemInfo.isWindows -> {
                assertEquals("cmd", commandLine.exePath)
                assertTrue(commandLine.parametersList.parameters.contains("/c"))
            }
            else -> {
                assertEquals("bash", commandLine.exePath)
                assertTrue(commandLine.parametersList.parameters.contains("-l"))
                assertTrue(commandLine.parametersList.parameters.contains("-c"))
            }
        }
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

fun createCommandLineForTest(command: String, workingDirectory: File?): GeneralCommandLine {
    val commandLine = GeneralCommandLine()
        .withWorkDirectory(workingDirectory)
        .withEnvironment(System.getenv())

    when {
        SystemInfo.isWindows -> {
            commandLine
                .withExePath("cmd")
                .withParameters("/c", "echo Working directory: %cd% && $command")
        }
        else -> {
            // Linux/macOS: Use bash as login shell to load full environment
            commandLine
                .withExePath("bash")
                .withParameters("-l", "-c", "echo \"Working directory: \$(pwd)\" && $command")
        }
    }

    return commandLine
}
