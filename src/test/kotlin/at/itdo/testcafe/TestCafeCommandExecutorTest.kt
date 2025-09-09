package at.itdo.testcafe

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.mockk

class TestCafeCommandExecutorTest : BasePlatformTestCase() {
    
    private lateinit var executor: TestCafeCommandExecutor
    
    override fun setUp() {
        super.setUp()
        executor = TestCafeCommandExecutor(project)
    }
    
    fun testBuildTestCommand() {
        val mockElement = mockk<PsiElement>()
        val testDeclaration = TestCafeDeclaration.Test(
            name = "Login test",
            element = mockElement,
            startOffset = 0,
            endOffset = 10
        )
        
        val filePath = "/path/to/test.spec.js"
        
        // Use reflection to test private method
        val method = TestCafeCommandExecutor::class.java
            .getDeclaredMethod("buildTestCommand", TestCafeDeclaration.Test::class.java, String::class.java)
        method.isAccessible = true
        
        val result = method.invoke(executor, testDeclaration, filePath) as String
        
        assertEquals("npx testcafe chrome /path/to/test.spec.js -t \"Login test\"", result)
    }
    
    fun testBuildFixtureCommand() {
        val mockElement = mockk<PsiElement>()
        val fixtureDeclaration = TestCafeDeclaration.Fixture(
            name = "Login Page",
            element = mockElement,
            startOffset = 0,
            endOffset = 10,
            page = "https://example.com"
        )
        
        val filePath = "/path/to/test.spec.js"
        
        // Use reflection to test private method
        val method = TestCafeCommandExecutor::class.java
            .getDeclaredMethod("buildFixtureCommand", TestCafeDeclaration.Fixture::class.java, String::class.java)
        method.isAccessible = true
        
        val result = method.invoke(executor, fixtureDeclaration, filePath) as String
        
        assertEquals("npx testcafe chrome /path/to/test.spec.js -f \"Login Page\"", result)
    }
    
    fun testBuildFileCommand() {
        val filePath = "/path/to/test.spec.js"
        
        // Use reflection to test private method
        val method = TestCafeCommandExecutor::class.java
            .getDeclaredMethod("buildFileCommand", String::class.java)
        method.isAccessible = true
        
        val result = method.invoke(executor, filePath) as String
        
        assertEquals("npx testcafe chrome /path/to/test.spec.js", result)
    }
}