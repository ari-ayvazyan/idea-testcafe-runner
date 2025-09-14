package at.itdo.tcrunner

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestOnlyDetectionTest {

    private val astAnalyzer = ASTAnalyzer()

    @Test
    fun `should detect fixture only usage`() {
        val code = """
            fixture.only('Priority Tests')
                .page('https://example.com');

            test('Some test', async t => {
                console.log('test');
            });
        """.trimIndent()

        assertTrue(astAnalyzer.hasOnlyUsage(code))
        val details = astAnalyzer.getOnlyUsageDetails(code)
        assertTrue(details.hasFixtureOnly)
        assertFalse(details.hasTestOnly)
        assertEquals(1, details.fixtureOnlyCount)
        assertEquals(0, details.testOnlyCount)
    }

    @Test
    fun `should detect test only usage`() {
        val code = """
            fixture('Regular Tests')
                .page('https://example.com');

            test.only('Run only this test', async t => {
                console.log('Only this test will run');
            });
        """.trimIndent()

        assertTrue(astAnalyzer.hasOnlyUsage(code))
        val details = astAnalyzer.getOnlyUsageDetails(code)
        assertFalse(details.hasFixtureOnly)
        assertTrue(details.hasTestOnly)
        assertEquals(0, details.fixtureOnlyCount)
        assertEquals(1, details.testOnlyCount)
    }

    @Test
    fun `should detect both fixture and test only usage`() {
        val code = """
            fixture.only('Priority Tests')
                .page('https://example.com');

            test.only('Run only this test', async t => {
                console.log('Only this test will run');
            });

            test.only('Another exclusive test', async t => {
                console.log('Another exclusive test');
            });
        """.trimIndent()

        assertTrue(astAnalyzer.hasOnlyUsage(code))
        val details = astAnalyzer.getOnlyUsageDetails(code)
        assertTrue(details.hasFixtureOnly)
        assertTrue(details.hasTestOnly)
        assertEquals(1, details.fixtureOnlyCount)
        assertEquals(2, details.testOnlyCount)
    }

    @Test
    fun `should not detect only usage in regular code`() {
        val code = """
            fixture('Regular Tests')
                .page('https://example.com');

            test('Regular test', async t => {
                console.log('Regular test');
            });

            test('Another test', async t => {
                console.log('Another test');
            });
        """.trimIndent()

        assertFalse(astAnalyzer.hasOnlyUsage(code))
        val details = astAnalyzer.getOnlyUsageDetails(code)
        assertFalse(details.hasFixtureOnly)
        assertFalse(details.hasTestOnly)
        assertEquals(0, details.fixtureOnlyCount)
        assertEquals(0, details.testOnlyCount)
    }

    @Test
    fun `should detect only usage in comments - current behavior`() {
        val code = """
            fixture('Regular Tests')
                .page('https://example.com');

            // test.only('Commented out test', async t => {
            //     console.log('This should not be detected');
            // });

            test('Regular test', async t => {
                console.log('Regular test');
            });
        """.trimIndent()

        // Note: Current implementation detects commented out code
        // This test documents current behavior - could be enhanced later to ignore comments
        assertTrue(astAnalyzer.hasOnlyUsage(code))
        val details = astAnalyzer.getOnlyUsageDetails(code)
        assertTrue(details.hasTestOnly)
        assertEquals(1, details.testOnlyCount)
    }

    @Test
    fun `should generate correct display messages`() {
        // Test only
        val testOnlyDetails = ASTAnalyzer.OnlyUsageInfo(
            hasTestOnly = true,
            hasFixtureOnly = false,
            testOnlyCount = 1,
            fixtureOnlyCount = 0
        )
        assertEquals("This file contains test.only() - only exclusive tests will run!", testOnlyDetails.getDisplayMessage())

        // Multiple test only
        val multipleTestOnlyDetails = ASTAnalyzer.OnlyUsageInfo(
            hasTestOnly = true,
            hasFixtureOnly = false,
            testOnlyCount = 3,
            fixtureOnlyCount = 0
        )
        assertEquals("This file contains test.only() (3 occurrences) - only exclusive tests will run!", multipleTestOnlyDetails.getDisplayMessage())

        // Fixture only
        val fixtureOnlyDetails = ASTAnalyzer.OnlyUsageInfo(
            hasTestOnly = false,
            hasFixtureOnly = true,
            testOnlyCount = 0,
            fixtureOnlyCount = 1
        )
        assertEquals("This file contains fixture.only() - only exclusive fixtures will run!", fixtureOnlyDetails.getDisplayMessage())

        // Both
        val bothDetails = ASTAnalyzer.OnlyUsageInfo(
            hasTestOnly = true,
            hasFixtureOnly = true,
            testOnlyCount = 2,
            fixtureOnlyCount = 1
        )
        assertEquals("This file contains test.only() and fixture.only() - only these exclusive tests will run!", bothDetails.getDisplayMessage())
    }
}