package at.itdo.tcrunner

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DuplicatePlayButtonTest {

    @Test
    fun `should not create duplicate declarations for fixture only`() {
        val code = """
            fixture.only('Priority Tests')
                .page('https://example.com');

            test('Some test', async t => {
                console.log('test');
            });
        """.trimIndent()

        val analyzer = ASTAnalyzer()

        // Use string-based testing to verify regex patterns work correctly
        val fixtureOnlyPattern = java.util.regex.Pattern.compile("fixture\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        val fixtureRegularPattern = java.util.regex.Pattern.compile("(?<!\\.)fixture(?!\\.only)\\s*\\(\\s*['\"]([^'\"]*)['\"]")

        val fixtureOnlyMatcher = fixtureOnlyPattern.matcher(code)
        val fixtureRegularMatcher = fixtureRegularPattern.matcher(code)

        // Should find one fixture.only match
        assertTrue(fixtureOnlyMatcher.find())
        assertEquals("Priority Tests", fixtureOnlyMatcher.group(1))

        // Should NOT find any regular fixture matches (since fixture.only should be excluded)
        assertFalse(fixtureRegularMatcher.find())

        // Test with the analyzer's usage detection
        assertTrue(analyzer.hasOnlyUsage(code))
        val details = analyzer.getOnlyUsageDetails(code)
        assertTrue(details.hasFixtureOnly)
        assertEquals(1, details.fixtureOnlyCount)
    }

    @Test
    fun `should not create duplicate declarations for test only`() {
        val code = """
            fixture('Regular Tests')
                .page('https://example.com');

            test.only('Priority test', async t => {
                console.log('priority test');
            });

            test('Regular test', async t => {
                console.log('regular test');
            });
        """.trimIndent()

        val testOnlyPattern = java.util.regex.Pattern.compile("test\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        val testRegularPattern = java.util.regex.Pattern.compile("(?<!\\.)test(?!\\.only)\\s*\\(\\s*['\"]([^'\"]*)['\"]")

        val testOnlyMatcher = testOnlyPattern.matcher(code)
        val testRegularMatcher = testRegularPattern.matcher(code)

        // Should find one test.only match
        assertTrue(testOnlyMatcher.find())
        assertEquals("Priority test", testOnlyMatcher.group(1))
        assertFalse(testOnlyMatcher.find()) // Should not find another test.only

        // Should find one regular test match (not the test.only one)
        assertTrue(testRegularMatcher.find())
        assertEquals("Regular test", testRegularMatcher.group(1))
        assertFalse(testRegularMatcher.find()) // Should not find another regular test
    }

    @Test
    fun `should correctly separate regular and only patterns`() {
        val code = """
            fixture('Regular Fixture')
                .page('https://example.com');

            fixture.only('Priority Fixture')
                .page('https://example.com/priority');

            test('Regular test', async t => {
                console.log('regular test');
            });

            test.only('Priority test', async t => {
                console.log('priority test');
            });
        """.trimIndent()

        val fixtureOnlyPattern = java.util.regex.Pattern.compile("fixture\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        val fixtureRegularPattern = java.util.regex.Pattern.compile("(?<!\\.)fixture(?!\\.only)\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        val testOnlyPattern = java.util.regex.Pattern.compile("test\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        val testRegularPattern = java.util.regex.Pattern.compile("(?<!\\.)test(?!\\.only)\\s*\\(\\s*['\"]([^'\"]*)['\"]")

        // Count matches for each pattern
        var fixtureOnlyCount = 0
        var fixtureRegularCount = 0
        var testOnlyCount = 0
        var testRegularCount = 0

        val fixtureOnlyMatcher = fixtureOnlyPattern.matcher(code)
        while (fixtureOnlyMatcher.find()) {
            fixtureOnlyCount++
        }

        val fixtureRegularMatcher = fixtureRegularPattern.matcher(code)
        while (fixtureRegularMatcher.find()) {
            fixtureRegularCount++
        }

        val testOnlyMatcher = testOnlyPattern.matcher(code)
        while (testOnlyMatcher.find()) {
            testOnlyCount++
        }

        val testRegularMatcher = testRegularPattern.matcher(code)
        while (testRegularMatcher.find()) {
            testRegularCount++
        }

        // Should find exactly one of each type
        assertEquals(1, fixtureOnlyCount, "Should find exactly 1 fixture.only")
        assertEquals(1, fixtureRegularCount, "Should find exactly 1 regular fixture")
        assertEquals(1, testOnlyCount, "Should find exactly 1 test.only")
        assertEquals(1, testRegularCount, "Should find exactly 1 regular test")

        // Total should be 4 distinct patterns
        assertEquals(4, fixtureOnlyCount + fixtureRegularCount + testOnlyCount + testRegularCount)
    }
}