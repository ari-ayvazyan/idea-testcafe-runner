package at.itdo.tcrunner

import at.itdo.tcrunner.run.parsing.LineType
import at.itdo.tcrunner.run.parsing.TestCafeLineClassifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnicodeTest {

    private val classifier = TestCafeLineClassifier()

    @Test
    fun `should classify and parse all platform test result symbols including ANSI colors`() {
        val ubuntuFailWithoutEmoji = " ✖ Test failure without emoji"
        val ubuntuFailWithEmoji = " ✖️ Test failure with emoji"
        val ubuntuSuccess = " ✓ Test success"
        val windowsFail = " × Windows failure"
        val windowsSuccess = " √ Windows success"

        val ansiColoredFail = "\u001B[31m ✖ ANSI colored failure\u001B[0m"
        val ansiColoredSuccess = "\u001B[32m ✓ ANSI colored success\u001B[0m"

        val testLines = listOf(
            ubuntuFailWithoutEmoji to false,
            ubuntuFailWithEmoji to false,
            ubuntuSuccess to true,
            windowsFail to false,
            windowsSuccess to true,
            ansiColoredFail to false,
            ansiColoredSuccess to true
        )

        for ((line, expectedSuccess) in testLines) {
            assertEquals(LineType.TEST_RESULT, classifier.classifyLine(line), "Line should be classified as TEST_RESULT: $line")
            val result = classifier.parseTestResult(line)
            assertNotNull(result, "Parsed result should not be null for: $line")
            assertEquals(expectedSuccess, result?.isSuccess, "Success state mismatch for: $line")
        }
    }

    @Test
    fun `should not classify test result lines as fixture lines`() {
        val ubuntuFail = " ✖ Test failure"
        val fixtureLine = " Sample Fixture"

        assertFalse(classifier.parseFixtureName(ubuntuFail) != null, "Fail line should not be parsed as fixture")
        assertEquals(LineType.FIXTURE, classifier.classifyLine(fixtureLine), "Fixture line should be classified as FIXTURE")
        assertEquals("Sample Fixture", classifier.parseFixtureName(fixtureLine))
    }
}
