package at.itdo.tcrunner

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TestCafeFileDetectorTest {

    @BeforeEach
    fun setUp() {
        // No setup needed for static functions
    }

    @Test
    fun testIsTestCafeFileWithSpecJs() {
        assertTrue(isTestCafeFileForTest("example.spec.js"))
    }

    @Test
    fun testIsTestCafeFileWithSpecTs() {
        assertTrue(isTestCafeFileForTest("example.spec.ts"))
    }

    @Test
    fun testIsTestCafeFileWithTestJs() {
        assertTrue(isTestCafeFileForTest("example.test.js"))
    }

    @Test
    fun testIsTestCafeFileWithDashTestTs() {
        assertTrue(isTestCafeFileForTest("example-test.ts"))
    }

    @Test
    fun testIsNotTestCafeFile() {
        assertFalse(isTestCafeFileForTest("regular.js"))
    }

    @Test
    fun testHasTestCafeContentWithFixture() {
        val content = """
            import { Selector } from 'testcafe';
            
            fixture('Login Page')
                .page('https://example.com/login');
            
            test('User can login', async t => {
                // test implementation
            });
        """.trimIndent()

        assertTrue(hasTestCafeContentForTest(content))
    }

    @Test
    fun testHasTestCafeContentWithTest() {
        val content = """
            test('Simple test', async t => {
                // test implementation
            });
        """.trimIndent()

        assertTrue(hasTestCafeContentForTest(content))
    }

    @Test
    fun testHasNoTestCafeContent() {
        val content = """
            function regularFunction() {
                console.log('Not a TestCafe file');
            }
        """.trimIndent()

        assertFalse(hasTestCafeContentForTest(content))
    }
}

// Helper functions for unit testing
fun isTestCafeFileForTest(fileName: String): Boolean {
    val defaultPatterns = setOf(
        "*.spec.js", "*.spec.ts", "*.test.js", "*.test.ts", "*-test.js", "*-test.ts"
    )
    
    return defaultPatterns.any { pattern ->
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
        Regex("^$regex$", RegexOption.IGNORE_CASE).matches(fileName)
    }
}

fun hasTestCafeContentForTest(content: String): Boolean {
    return content.contains("fixture(") || content.contains("test(")
}