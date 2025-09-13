package at.itdo.tcrunner.run.parsing

/**
 * Interface for emitting test events, allowing different implementations for testing vs production
 */
interface TestEventEmitter {
    fun emitTestRunStarted()
    fun emitTestRunFinished()
    fun emitFixtureStarted(fixtureName: String, filePath: String)
    fun emitFixtureFinished(fixtureName: String)
    fun emitTestStarted(testName: String, filePath: String)
    fun emitTestPassed(testName: String, messages: String = "")
    fun emitTestFailed(testName: String, messages: String = "")
}
