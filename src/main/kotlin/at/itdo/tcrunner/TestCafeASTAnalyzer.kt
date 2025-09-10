package at.itdo.tcrunner

import com.intellij.psi.PsiFile
import java.util.regex.Pattern

class TestCafeASTAnalyzer {

    companion object {
        private val FIXTURE_PATTERN = Pattern.compile("fixture\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        private val TEST_PATTERN = Pattern.compile("test\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        private val PAGE_PATTERN = Pattern.compile("\\.page\\s*\\(\\s*['\"]([^'\"]*)['\"]")
    }

    fun findTestCafeDeclarations(psiFile: PsiFile): List<TestCafeDeclaration> {
        val declarations = mutableListOf<TestCafeDeclaration>()
        val text = psiFile.text

        // Find fixtures
        val fixtureMatcher = FIXTURE_PATTERN.matcher(text)
        while (fixtureMatcher.find()) {
            val name = fixtureMatcher.group(1)
            val startOffset = fixtureMatcher.start()
            val endOffset = fixtureMatcher.end()

            // Look for page information after this fixture
            val page = findPageAfterPosition(text, endOffset)

            val element = psiFile.findElementAt(startOffset) ?: continue

            declarations.add(TestCafeDeclaration.Fixture(
                name = name,
                element = element,
                startOffset = startOffset,
                endOffset = endOffset,
                page = page
            ))
        }

        // Find tests
        val testMatcher = TEST_PATTERN.matcher(text)
        while (testMatcher.find()) {
            val name = testMatcher.group(1)
            val startOffset = testMatcher.start()
            val endOffset = testMatcher.end()

            val element = psiFile.findElementAt(startOffset) ?: continue

            declarations.add(TestCafeDeclaration.Test(
                name = name,
                element = element,
                startOffset = startOffset,
                endOffset = endOffset
            ))
        }

        return declarations
    }

    private fun findPageAfterPosition(text: String, startPosition: Int): String? {
        val remainingText = text.substring(startPosition)
        val matcher = PAGE_PATTERN.matcher(remainingText)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
}
