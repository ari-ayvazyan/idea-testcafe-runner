package at.itdo.tcrunner

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk

class TestCafeFileDetectorTest : BasePlatformTestCase() {

    private lateinit var detector: TestCafeFileDetector

    override fun setUp() {
        super.setUp()
        detector = TestCafeFileDetector(project)
    }

    fun testIsTestCafeFileWithSpecJs() {
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example.spec.js"

        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    fun testIsTestCafeFileWithSpecTs() {
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example.spec.ts"

        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    fun testIsTestCafeFileWithTestJs() {
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example.test.js"

        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    fun testIsTestCafeFileWithDashTestTs() {
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example-test.ts"

        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    fun testIsNotTestCafeFile() {
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "regular.js"

        assertFalse(detector.isTestCafeFile(virtualFile))
    }

    fun testHasTestCafeContentWithFixture() {
        val psiFile = mockk<PsiFile>()
        every { psiFile.text } returns """
            import { Selector } from 'testcafe';
            
            fixture('Login Page')
                .page('https://example.com/login');
            
            test('User can login', async t => {
                // test implementation
            });
        """.trimIndent()

        assertTrue(detector.hasTestCafeContent(psiFile))
    }

    fun testHasTestCafeContentWithTest() {
        val psiFile = mockk<PsiFile>()
        every { psiFile.text } returns """
            test('Simple test', async t => {
                // test implementation
            });
        """.trimIndent()

        assertTrue(detector.hasTestCafeContent(psiFile))
    }

    fun testHasNoTestCafeContent() {
        val psiFile = mockk<PsiFile>()
        every { psiFile.text } returns """
            function regularFunction() {
                console.log('Not a TestCafe file');
            }
        """.trimIndent()

        assertFalse(detector.hasTestCafeContent(psiFile))
    }
}
