package at.itdo.tcrunner.run.parsing

enum class LineType {
    TEST_EXECUTION_START,
    TEST_RESULT,
    FIXTURE,
    TEST_RUN_SUMMARY,
    ERROR_MESSAGE,
    REGULAR_MESSAGE,
    EMPTY
}

data class TestResult(val isSuccess: Boolean, val testName: String)
data class TestRunSummary(val passed: Int, val total: Int, val duration: String)

class TestCafeLineClassifier {

    companion object {
        private val ANSI_PATTERN = Regex("\u001B\\[[0-9;?]*[a-zA-Z]")
        private val TEST_RESULT_PATTERN = Regex("^ (√|×|✖️|✖|✓) (.*)$")
        private val SUMMARY_FAILED_PATTERN = Regex("(\\d+)/(\\d+)\\s+failed\\s+\\(([^)]+)\\)")
        private val SUMMARY_PASSED_PATTERN = Regex("(\\d+)\\s+passed\\s+\\(([^)]+)\\)")

        fun stripAnsi(text: String): String {
            return text.replace(ANSI_PATTERN, "")
        }
    }

    fun classifyLine(rawLine: String): LineType {
        val line = stripAnsi(rawLine)
        val trimmed = line.trim()

        return when {
            trimmed.isEmpty() -> LineType.EMPTY
            isTestExecutionStart(line) -> LineType.TEST_EXECUTION_START
            isTestResult(line) -> LineType.TEST_RESULT
            isTestRunSummary(line) -> LineType.TEST_RUN_SUMMARY
            isFixtureLine(line) -> LineType.FIXTURE
            isErrorMessage(line) -> LineType.ERROR_MESSAGE
            else -> LineType.REGULAR_MESSAGE
        }
    }

    fun parseTestResult(rawLine: String): TestResult? {
        val line = stripAnsi(rawLine)
        val match = TEST_RESULT_PATTERN.find(line) ?: return null

        val symbol = match.groupValues[1]
        val testName = match.groupValues[2].trim()
        val isSuccess = symbol == "√" || symbol == "✓"

        return TestResult(isSuccess, testName)
    }

    fun parseFixtureName(rawLine: String): String? {
        val line = stripAnsi(rawLine)
        return if (isFixtureLine(line)) line.trim() else null
    }

    fun parseTestRunSummary(rawLine: String): TestRunSummary? {
        val line = stripAnsi(rawLine)
        val trimmed = line.trim()

        val failedMatch = SUMMARY_FAILED_PATTERN.find(trimmed)
        if (failedMatch != null) {
            val failed = failedMatch.groupValues[1].toInt()
            val total = failedMatch.groupValues[2].toInt()
            val duration = failedMatch.groupValues[3]
            return TestRunSummary(total - failed, total, duration)
        }

        val passedMatch = SUMMARY_PASSED_PATTERN.find(trimmed)
        if (passedMatch != null) {
            val passed = passedMatch.groupValues[1].toInt()
            val duration = passedMatch.groupValues[2]
            return TestRunSummary(passed, passed, duration)
        }

        return null
    }

    private fun isTestExecutionStart(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.isNotEmpty() &&
               (trimmed.startsWith("Running tests in:") || (!line.startsWith(" ") && trimmed.startsWith("Running tests")))
    }

    private fun isTestResult(line: String): Boolean {
        return TEST_RESULT_PATTERN.containsMatchIn(line)
    }

    private fun isTestRunSummary(line: String): Boolean {
        val trimmed = line.trim()
        return SUMMARY_FAILED_PATTERN.containsMatchIn(trimmed) ||
               SUMMARY_PASSED_PATTERN.containsMatchIn(trimmed)
    }

    private fun isFixtureLine(line: String): Boolean {
        val trimmed = line.trim()
        return line.startsWith(" ") &&
               !line.startsWith("  ") &&
               !isTestResult(line) &&
               !isTestRunSummary(line) &&
               !trimmed.startsWith("Running tests") &&
               !trimmed.startsWith("Browser:") &&
               !line.matches(Regex("\\s*\\d+\\).*")) &&
               !trimmed.startsWith("at ") &&
               trimmed != "--"
    }

    private fun isErrorMessage(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.isNotBlank() &&
               !trimmed.contains("Browser:") &&
               !line.matches(Regex("\\s*\\d+\\).*")) &&
               !line.matches(Regex("\\s*\\d+\\s\\|.*")) &&
               !line.matches(Regex("\\s*>\\s\\d+\\s\\|.*")) &&
               !trimmed.startsWith("at ")
    }
}
