package at.itdo.tcrunner.run.parsing

/**
 * Interface for emitting test events, allowing different implementations for testing vs production
 */
interface TestEventEmitter {
    fun emitTestRunStarted()
    fun emitTestRunFinished(duration: String = "")
    fun emitFixtureStarted(fixtureName: String, filePath: String)
    fun emitFixtureFinished(fixtureName: String, duration: String = "")
    fun emitTestStarted(testName: String, filePath: String)
    fun emitTestPassed(testName: String, messages: String = "", duration: String = "")
    fun emitTestFailed(testName: String, messages: String = "", duration: String = "")
}
