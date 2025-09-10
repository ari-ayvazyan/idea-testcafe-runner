package at.itdo.tcrunner

import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TestCafeASTAnalyzerTest : BasePlatformTestCase() {

    private lateinit var analyzer: TestCafeASTAnalyzer

    override fun setUp() {
        super.setUp()
        analyzer = TestCafeASTAnalyzer()
    }

    fun testFindFixtureDeclaration() {
        val content = """
            fixture('Login Tests')
                .page('https://example.com/login');
            
            test('User can login', async t => {
                // test content
            });
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("test.spec.js", PlainTextFileType.INSTANCE, content)

        val declarations = analyzer.findTestCafeDeclarations(psiFile)

        assertEquals(2, declarations.size)

        val fixture = declarations.find { it is TestCafeDeclaration.Fixture } as TestCafeDeclaration.Fixture?
        assertNotNull(fixture)
        assertEquals("Login Tests", fixture!!.name)
        assertEquals("https://example.com/login", fixture.page)

        val test = declarations.find { it is TestCafeDeclaration.Test } as TestCafeDeclaration.Test?
        assertNotNull(test)
        assertEquals("User can login", test!!.name)
    }

    fun testFindMultipleTests() {
        val content = """
            fixture('API Tests');
            
            test('First test', async t => {
                // test 1
            });
            
            test('Second test', async t => {
                // test 2
            });
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("api.spec.js", PlainTextFileType.INSTANCE, content)

        val declarations = analyzer.findTestCafeDeclarations(psiFile)

        assertEquals(3, declarations.size)

        val fixtures = declarations.filterIsInstance<TestCafeDeclaration.Fixture>()
        assertEquals(1, fixtures.size)
        assertEquals("API Tests", fixtures[0].name)

        val tests = declarations.filterIsInstance<TestCafeDeclaration.Test>()
        assertEquals(2, tests.size)
        assertEquals("First test", tests[0].name)
        assertEquals("Second test", tests[1].name)
    }

    fun testEmptyFile() {
        val content = """
            console.log('No TestCafe content here');
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("regular.js", PlainTextFileType.INSTANCE, content)

        val declarations = analyzer.findTestCafeDeclarations(psiFile)

        assertEquals(0, declarations.size)
    }

    fun testFixtureWithoutPage() {
        val content = """
            fixture('Simple fixture');
            
            test('Simple test', async t => {
                // test content
            });
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("simple.spec.js", PlainTextFileType.INSTANCE, content)

        val declarations = analyzer.findTestCafeDeclarations(psiFile)

        val fixture = declarations.filterIsInstance<TestCafeDeclaration.Fixture>().firstOrNull()
        assertNotNull(fixture)
        assertEquals("Simple fixture", fixture!!.name)
        assertNull(fixture.page)
    }
}
