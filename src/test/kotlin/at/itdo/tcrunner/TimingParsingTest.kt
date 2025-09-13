package at.itdo.tcrunner

import at.itdo.tcrunner.run.parsing.TestCafeLineClassifier
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TimingParsingTest {

    private val classifier = TestCafeLineClassifier()

    @Test
    fun `should parse failed test summary with timing`() {
        val line = " 2/5 failed (2s)"
        val summary = classifier.parseTestRunSummary(line)

        assertNotNull(summary)
        assertEquals(3, summary.passed) // 5 total - 2 failed = 3 passed
        assertEquals(5, summary.total)
        assertEquals("2s", summary.duration)
    }

    @Test
    fun `should parse passed test summary with timing`() {
        val line = " 3 passed (1s)"
        val summary = classifier.parseTestRunSummary(line)

        assertNotNull(summary)
        assertEquals(3, summary.passed)
        assertEquals(3, summary.total)
        assertEquals("1s", summary.duration)
    }

    @Test
    fun `should parse summary with milliseconds`() {
        val line = " 1/2 failed (500ms)"
        val summary = classifier.parseTestRunSummary(line)

        assertNotNull(summary)
        assertEquals(1, summary.passed)
        assertEquals(2, summary.total)
        assertEquals("500ms", summary.duration)
    }

    @Test
    fun `should parse summary with decimal seconds`() {
        val line = " 5 passed (1.5s)"
        val summary = classifier.parseTestRunSummary(line)

        assertNotNull(summary)
        assertEquals(5, summary.passed)
        assertEquals(5, summary.total)
        assertEquals("1.5s", summary.duration)
    }
}