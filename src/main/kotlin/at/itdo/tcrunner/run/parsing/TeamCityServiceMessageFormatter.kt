package at.itdo.tcrunner.run.parsing

/**
 * Utility for formatting TeamCity service messages
 */
object TeamCityServiceMessageFormatter {

    fun testRunStarted(): String = "##teamcity[enteredTheMatrix]"

    fun testSuiteStarted(name: String, locationHint: String? = null): String {
        val location = locationHint?.let { " locationHint='$it'" } ?: ""
        return "##teamcity[testSuiteStarted name='${escapeValue(name)}'$location]"
    }

    fun testSuiteFinished(name: String): String =
        "##teamcity[testSuiteFinished name='${escapeValue(name)}']"

    fun testStarted(name: String, locationHint: String? = null): String {
        val location = locationHint?.let { " locationHint='$it'" } ?: ""
        return "##teamcity[testStarted name='${escapeValue(name)}'$location]"
    }

    fun testFinished(name: String, duration: String = "0"): String =
        "##teamcity[testFinished name='${escapeValue(name)}' duration='$duration']"

    fun testFailed(name: String): String =
        "##teamcity[testFailed name='${escapeValue(name)}' message='Test failed']"

    fun testStdOut(name: String, out: String): String =
        "##teamcity[testStdOut name='${escapeValue(name)}' out='${escapeValue(out)}']"

    fun buildProblem(description: String, identity: String): String =
        "##teamcity[buildProblem description='${escapeValue(description)}' identity='${escapeValue(identity)}']"

    private fun escapeValue(value: String): String {
        return value
            .replace("|", "||")
            .replace("'", "|'")
            .replace("\n", "|n")
            .replace("\r", "|r")
            .replace("[", "|[")
            .replace("]", "|]")
    }
}
