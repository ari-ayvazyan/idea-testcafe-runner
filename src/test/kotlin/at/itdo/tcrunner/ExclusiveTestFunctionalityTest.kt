package at.itdo.tcrunner

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExclusiveTestFunctionalityTest {

    private val astAnalyzer = ASTAnalyzer()

    @Test
    fun `should detect fixture only patterns correctly`() {
        val codeWithFixtureOnly = """
            fixture.only('Priority Tests')
                .page('https://example.com');

            test('Some test', async t => {
                console.log('test');
            });
        """.trimIndent()

        assertTrue(astAnalyzer.hasOnlyUsage(codeWithFixtureOnly))
        val details = astAnalyzer.getOnlyUsageDetails(codeWithFixtureOnly)
        assertTrue(details.hasFixtureOnly)
        assertFalse(details.hasTestOnly)
        assertEquals(1, details.fixtureOnlyCount)
        assertEquals(0, details.testOnlyCount)
    }

    @Test
    fun `should detect test only patterns correctly`() {
        val codeWithTestOnly = """
            fixture('Regular Tests')
                .page('https://example.com');

            test.only('Priority test', async t => {
                console.log('priority test');
            });

            test('Regular test', async t => {
                console.log('regular test');
            });
        """.trimIndent()

        assertTrue(astAnalyzer.hasOnlyUsage(codeWithTestOnly))
        val details = astAnalyzer.getOnlyUsageDetails(codeWithTestOnly)
        assertFalse(details.hasFixtureOnly)
        assertTrue(details.hasTestOnly)
        assertEquals(0, details.fixtureOnlyCount)
        assertEquals(1, details.testOnlyCount)
    }

    @Test
    fun `should detect multiple test only patterns`() {
        val codeWithMultipleTestOnly = """
            fixture('Regular Tests')
                .page('https://example.com');

            test.only('Priority test 1', async t => {
                console.log('priority test 1');
            });

            test('Regular test', async t => {
                console.log('regular test');
            });

            test.only('Priority test 2', async t => {
                console.log('priority test 2');
            });
        """.trimIndent()

        assertTrue(astAnalyzer.hasOnlyUsage(codeWithMultipleTestOnly))
        val details = astAnalyzer.getOnlyUsageDetails(codeWithMultipleTestOnly)
        assertFalse(details.hasFixtureOnly)
        assertTrue(details.hasTestOnly)
        assertEquals(0, details.fixtureOnlyCount)
        assertEquals(2, details.testOnlyCount)
    }

    @Test
    fun `should detect mixed fixture and test only patterns`() {
        val codeWithMixed = """
            fixture.only('Priority Fixture')
                .page('https://example.com');

            test('Test in priority fixture', async t => {
                console.log('test in priority fixture');
            });

            fixture('Regular Fixture')
                .page('https://example.com/other');

            test.only('Priority test', async t => {
                console.log('priority test');
            });

            test('Regular test', async t => {
                console.log('regular test');
            });
        """.trimIndent()

        assertTrue(astAnalyzer.hasOnlyUsage(codeWithMixed))
        val details = astAnalyzer.getOnlyUsageDetails(codeWithMixed)
        assertTrue(details.hasFixtureOnly)
        assertTrue(details.hasTestOnly)
        assertEquals(1, details.fixtureOnlyCount)
        assertEquals(1, details.testOnlyCount)
    }

    @Test
    fun `should not detect only patterns in regular code`() {
        val regularCode = """
            fixture('Regular Tests')
                .page('https://example.com');

            test('Regular test 1', async t => {
                console.log('regular test 1');
            });

            test('Regular test 2', async t => {
                console.log('regular test 2');
            });
        """.trimIndent()

        assertFalse(astAnalyzer.hasOnlyUsage(regularCode))
        val details = astAnalyzer.getOnlyUsageDetails(regularCode)
        assertFalse(details.hasFixtureOnly)
        assertFalse(details.hasTestOnly)
        assertEquals(0, details.fixtureOnlyCount)
        assertEquals(0, details.testOnlyCount)
    }

    @Test
    fun `should provide consistent display messages for exclusive tests`() {
        val testOnlyCode = "test.only('some test', async t => {});"
        val fixtureOnlyCode = "fixture.only('some fixture').page('http://example.com');"
        val mixedCode = "test.only('test', async t => {}); fixture.only('fixture').page('http://example.com');"

        val testOnlyDetails = astAnalyzer.getOnlyUsageDetails(testOnlyCode)
        val fixtureOnlyDetails = astAnalyzer.getOnlyUsageDetails(fixtureOnlyCode)
        val mixedDetails = astAnalyzer.getOnlyUsageDetails(mixedCode)

        assertEquals("This file contains test.only() or fixture.only() - only these tests can be run!",
                    testOnlyDetails.getDisplayMessage())
        assertEquals("This file contains test.only() or fixture.only() - only these tests can be run!",
                    fixtureOnlyDetails.getDisplayMessage())
        assertEquals("This file contains test.only() or fixture.only() - only these tests can be run!",
                    mixedDetails.getDisplayMessage())
    }
}