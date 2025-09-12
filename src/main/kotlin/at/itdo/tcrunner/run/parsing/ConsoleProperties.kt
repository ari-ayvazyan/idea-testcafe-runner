package at.itdo.tcrunner.run.parsing

import at.itdo.tcrunner.run.RunConfiguration
import com.intellij.execution.Executor
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTestLocator

class TestCafeTestConsoleProperties(
    configuration: RunConfiguration,
    executor: Executor
) : SMTRunnerConsoleProperties(configuration, "TestCafe", executor) {

    override fun getTestLocator(): SMTestLocator {
        return TestLocator()
    }

    override fun isIdBasedTestTree(): Boolean = false

    init {
        // Configure test console behavior
        setIfUndefined(HIDE_PASSED_TESTS, false)
        setIfUndefined(HIDE_IGNORED_TEST, false)
        setIfUndefined(SCROLL_TO_STACK_TRACE, true)
        setIfUndefined(SELECT_FIRST_DEFECT, true)
        setIfUndefined(TRACK_RUNNING_TEST, true)
    }
}
