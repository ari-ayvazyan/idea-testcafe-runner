package at.itdo.tcrunner

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TestCafeFileDetectorTest {

    private lateinit var project: Project
    private lateinit var settings: TestCafeSettings
    private lateinit var detector: TestCafeFileDetector

    @BeforeEach
    fun setUp() {
        project = mockk<Project>()
        settings = mockk<TestCafeSettings>()
        
        // Mock the static method TestCafeSettings.getInstance
        mockkStatic(TestCafeSettings::class)
        every { TestCafeSettings.getInstance(project) } returns settings
        
        detector = TestCafeFileDetector(project)
    }
    
    @AfterEach
    fun tearDown() {
        unmockkStatic(TestCafeSettings::class)
    }

    @Test
    fun testIsTestCafeFileWithSpecJs() {
        // Setup mock to return default patterns
        every { settings.getFilePatternsAsSet() } returns emptySet()
        
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example.spec.js"
        
        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    @Test
    fun testIsTestCafeFileWithSpecTs() {
        every { settings.getFilePatternsAsSet() } returns emptySet()
        
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example.spec.ts"
        
        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    @Test
    fun testIsTestCafeFileWithTestJs() {
        every { settings.getFilePatternsAsSet() } returns emptySet()
        
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example.test.js"
        
        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    @Test
    fun testIsTestCafeFileWithDashTestTs() {
        every { settings.getFilePatternsAsSet() } returns emptySet()
        
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "example-test.ts"
        
        assertTrue(detector.isTestCafeFile(virtualFile))
    }

    @Test
    fun testIsNotTestCafeFile() {
        every { settings.getFilePatternsAsSet() } returns emptySet()
        
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "regular.js"
        
        assertFalse(detector.isTestCafeFile(virtualFile))
    }

    @Test
    fun testHasTestCafeContentWithFixture() {
        val content = """
            import { Selector } from 'testcafe';
            
            fixture('Login Page')
                .page('https://example.com/login');
            
            test('User can login', async t => {
                // test implementation
            });
        """.trimIndent()

        val psiFile = mockk<PsiFile>()
        every { psiFile.text } returns content

        assertTrue(detector.hasTestCafeContent(psiFile))
    }

    @Test
    fun testHasTestCafeContentWithTest() {
        val content = """
            test('Simple test', async t => {
                // test implementation
            });
        """.trimIndent()

        val psiFile = mockk<PsiFile>()
        every { psiFile.text } returns content

        assertTrue(detector.hasTestCafeContent(psiFile))
    }

    @Test
    fun testHasNoTestCafeContent() {
        val content = """
            function regularFunction() {
                console.log('Not a TestCafe file');
            }
        """.trimIndent()

        val psiFile = mockk<PsiFile>()
        every { psiFile.text } returns content

        assertFalse(detector.hasTestCafeContent(psiFile))
    }

    @Test
    fun testRegexPatternsMatchCorrectFiles() {
        every { settings.getFilePatternsAsSet() } returns emptySet()
        
        // Test various valid TestCafe file names
        val validFiles = listOf(
            "login.spec.js", "HomePage.spec.ts", "user-management.test.js",
            "PAYMENT.TEST.TS", "auth-test.js", "validation-test.ts"
        )
        
        validFiles.forEach { fileName ->
            val virtualFile = mockk<VirtualFile>()
            every { virtualFile.name } returns fileName
            assertTrue(detector.isTestCafeFile(virtualFile), "Expected $fileName to match")
        }

        // Test files that should NOT match
        val invalidFiles = listOf(
            "regular.js", "component.tsx", "helper.spec.jsx",
            "test.config.js", "spec-helper.js"
        )
        
        invalidFiles.forEach { fileName ->
            val virtualFile = mockk<VirtualFile>()
            every { virtualFile.name } returns fileName
            assertFalse(detector.isTestCafeFile(virtualFile), "Expected $fileName NOT to match")
        }
    }
    @Test
    fun testCustomRegexPatterns() {
        // Test with custom regex patterns from settings
        val customPatterns = setOf(".*\\.e2e\\.(js|ts)$", ".*\\.integration\\.(js|ts)$")
        every { settings.getFilePatternsAsSet() } returns customPatterns
        
        val validFile = mockk<VirtualFile>()
        every { validFile.name } returns "login.e2e.js"
        assertTrue(detector.isTestCafeFile(validFile))
        
        val invalidFile = mockk<VirtualFile>()
        every { invalidFile.name } returns "login.spec.js"
        assertFalse(detector.isTestCafeFile(invalidFile))
    }

    @Test
    fun testPsiFileDetection() {
        every { settings.getFilePatternsAsSet() } returns emptySet()
        
        val virtualFile = mockk<VirtualFile>()
        every { virtualFile.name } returns "test.spec.js"
        
        val psiFile = mockk<PsiFile>()
        every { psiFile.virtualFile } returns virtualFile
        
        assertTrue(detector.isTestCafeFile(psiFile))
    }

    @Test
    fun testPsiFileDetectionWithNullVirtualFile() {
        val psiFile = mockk<PsiFile>()
        every { psiFile.virtualFile } returns null
        
        assertFalse(detector.isTestCafeFile(psiFile))
    }
}
