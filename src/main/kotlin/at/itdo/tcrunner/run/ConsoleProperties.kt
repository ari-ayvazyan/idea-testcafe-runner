package at.itdo.tcrunner.run

import com.intellij.execution.Executor
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTestLocator

class TestCafeTestConsoleProperties(
    configuration: RunConfiguration,
    executor: Executor
) : SMTRunnerConsoleProperties(configuration, "TestCafe", executor) {

    override fun getTestLocator(): SMTestLocator {
        return TestCafeTestLocator()
    }

    override fun isIdBasedTestTree(): Boolean = false
    
    init {
        // Configure test console behavior
        setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false)
        setIfUndefined(TestConsoleProperties.HIDE_IGNORED_TEST, false) 
        setIfUndefined(TestConsoleProperties.SCROLL_TO_STACK_TRACE, true)
        setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, true)
        setIfUndefined(TestConsoleProperties.TRACK_RUNNING_TEST, true)
    }
}