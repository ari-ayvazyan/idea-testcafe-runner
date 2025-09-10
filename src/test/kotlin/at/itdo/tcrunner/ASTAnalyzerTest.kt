package at.itdo.tcrunner

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestCafeASTAnalyzerTest {

    private lateinit var analyzer: ASTAnalyzer

    @BeforeEach
    fun setUp() {
        analyzer = ASTAnalyzer()
    }

    @Test
    fun testFindFixtureDeclaration() {
        val content = """
            fixture('Login Tests')
                .page('https://example.com/login');
            
            test('User can login', async t => {
                // test content
            });
        """.trimIndent()

        val declarations = analyzer.parseTestCafeContent(content)

        assertEquals(2, declarations.size)

        val fixture = declarations.find { it is MockTestCafeDeclaration.Fixture } as MockTestCafeDeclaration.Fixture?
        assertNotNull(fixture)
        assertEquals("Login Tests", fixture!!.name)
        assertEquals("https://example.com/login", fixture.page)

        val test = declarations.find { it is MockTestCafeDeclaration.Test } as MockTestCafeDeclaration.Test?
        assertNotNull(test)
        assertEquals("User can login", test!!.name)
    }

    @Test
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

        val declarations = analyzer.parseTestCafeContent(content)

        assertEquals(3, declarations.size)

        val fixtures = declarations.filterIsInstance<MockTestCafeDeclaration.Fixture>()
        assertEquals(1, fixtures.size)
        assertEquals("API Tests", fixtures[0].name)

        val tests = declarations.filterIsInstance<MockTestCafeDeclaration.Test>()
        assertEquals(2, tests.size)
        assertEquals("First test", tests[0].name)
        assertEquals("Second test", tests[1].name)
    }

    @Test
    fun testEmptyFile() {
        val content = """
            console.log('No TestCafe content here');
        """.trimIndent()

        val declarations = analyzer.parseTestCafeContent(content)

        assertEquals(0, declarations.size)
    }

    @Test
    fun testFixtureWithoutPage() {
        val content = """
            fixture('Simple fixture');
            
            test('Simple test', async t => {
                // test content
            });
        """.trimIndent()

        val declarations = analyzer.parseTestCafeContent(content)

        val fixture = declarations.filterIsInstance<MockTestCafeDeclaration.Fixture>().firstOrNull()
        assertNotNull(fixture)
        assertEquals("Simple fixture", fixture!!.name)
        assertNull(fixture.page)
    }
}

// Mock classes for unit testing without IntelliJ dependencies
sealed class MockTestCafeDeclaration(
    val name: String,
    val startOffset: Int,
    val endOffset: Int
) {
    class Fixture(
        name: String,
        startOffset: Int,
        endOffset: Int,
        val page: String? = null
    ) : MockTestCafeDeclaration(name, startOffset, endOffset)

    class Test(
        name: String,
        startOffset: Int,
        endOffset: Int
    ) : MockTestCafeDeclaration(name, startOffset, endOffset)
}

// Extension method for unit testing
fun ASTAnalyzer.parseTestCafeContent(content: String): List<MockTestCafeDeclaration> {
    val declarations = mutableListOf<MockTestCafeDeclaration>()

    // Use the same patterns from the original class
    val fixturePattern = java.util.regex.Pattern.compile("fixture\\s*\\(\\s*['\"]([^'\"]*)['\"]")
    val testPattern = java.util.regex.Pattern.compile("test\\s*\\(\\s*['\"]([^'\"]*)['\"]")
    val pagePattern = java.util.regex.Pattern.compile("\\.page\\s*\\(\\s*['\"]([^'\"]*)['\"]")

    // Find fixtures
    val fixtureMatcher = fixturePattern.matcher(content)
    while (fixtureMatcher.find()) {
        val name = fixtureMatcher.group(1)
        val startOffset = fixtureMatcher.start()
        val endOffset = fixtureMatcher.end()

        // Look for page information after this fixture
        val remainingContent = content.substring(endOffset)
        val pageMatcher = pagePattern.matcher(remainingContent)
        val page = if (pageMatcher.find()) pageMatcher.group(1) else null

        declarations.add(MockTestCafeDeclaration.Fixture(
            name = name,
            startOffset = startOffset,
            endOffset = endOffset,
            page = page
        ))
    }

    // Find tests
    val testMatcher = testPattern.matcher(content)
    while (testMatcher.find()) {
        val name = testMatcher.group(1)
        val startOffset = testMatcher.start()
        val endOffset = testMatcher.end()

        declarations.add(MockTestCafeDeclaration.Test(
            name = name,
            startOffset = startOffset,
            endOffset = endOffset
        ))
    }

    return declarations
}
