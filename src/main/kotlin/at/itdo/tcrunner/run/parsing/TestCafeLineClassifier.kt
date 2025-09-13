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

class TestCafeLineClassifier {

    fun classifyLine(line: String): LineType {
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

    fun parseTestResult(line: String): TestResult? {
        if (!isTestResult(line)) return null

        val isSuccess = line.contains("√")
        val testName = line.substring(3).trim() // Remove " √ " or " × "
        return TestResult(isSuccess, testName)
    }

    fun parseFixtureName(line: String): String? {
        return if (isFixtureLine(line)) line.trim() else null
    }

    private fun isTestExecutionStart(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.isNotEmpty() &&
               (trimmed.startsWith("Running tests in:") || !line.startsWith(" "))
    }

    private fun isTestResult(line: String): Boolean {
        return line.matches(Regex(" [√×] .*"))
    }

    private fun isTestRunSummary(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.matches(Regex("\\d+/\\d+\\s+failed.*")) ||
               trimmed.matches(Regex("\\d+\\s+passed \\(.+\\)"))
    }

    private fun isFixtureLine(line: String): Boolean {
        val trimmed = line.trim()
        return line.startsWith(" ") &&
               !line.startsWith("  ") &&
               !line.matches(Regex(" [√×] .*")) &&
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