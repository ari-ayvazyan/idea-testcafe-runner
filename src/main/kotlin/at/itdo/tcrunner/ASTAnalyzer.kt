package at.itdo.tcrunner

import com.intellij.psi.PsiFile
import java.util.regex.Pattern

class ASTAnalyzer {

    companion object {
        private val FIXTURE_PATTERN = Pattern.compile("fixture\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        private val FIXTURE_ONLY_PATTERN_WITH_NAME = Pattern.compile("fixture\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        private val TEST_PATTERN = Pattern.compile("test\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        private val TEST_ONLY_PATTERN_WITH_NAME = Pattern.compile("test\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        private val PAGE_PATTERN = Pattern.compile("\\.page\\s*\\(\\s*['\"]([^'\"]*)['\"]")
        private val FIXTURE_ONLY_PATTERN = Pattern.compile("fixture\\.only\\s*\\(")
        private val TEST_ONLY_PATTERN = Pattern.compile("test\\.only\\s*\\(")
    }

    fun findTestCafeDeclarations(psiFile: PsiFile): List<Declaration> {
        val declarations = mutableListOf<Declaration>()
        val text = psiFile.text

        // Find fixtures (including fixture.only)
        val fixtureOnlyMatcher = FIXTURE_ONLY_PATTERN_WITH_NAME.matcher(text)
        while (fixtureOnlyMatcher.find()) {
            val name = fixtureOnlyMatcher.group(1)
            val startOffset = fixtureOnlyMatcher.start()
            val endOffset = fixtureOnlyMatcher.end()

            // Look for page information after this fixture
            val page = findPageAfterPosition(text, endOffset)

            val element = psiFile.findElementAt(startOffset) ?: continue

            declarations.add(
                Declaration.Fixture(
                    name = name,
                    element = element,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    page = page,
                    isExclusive = true
                )
            )
        }

        // Find regular fixtures (not .only)
        val fixtureMatcher = FIXTURE_PATTERN.matcher(text)
        while (fixtureMatcher.find()) {
            val name = fixtureMatcher.group(1)
            val startOffset = fixtureMatcher.start()
            val endOffset = fixtureMatcher.end()

            // Skip if this is already covered by fixture.only
            if (declarations.any { it.startOffset == startOffset && it is Declaration.Fixture }) {
                continue
            }

            // Look for page information after this fixture
            val page = findPageAfterPosition(text, endOffset)

            val element = psiFile.findElementAt(startOffset) ?: continue

            declarations.add(
                Declaration.Fixture(
                    name = name,
                    element = element,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    page = page,
                    isExclusive = false
                )
            )
        }

        // Find tests (including test.only)
        val testOnlyMatcher = TEST_ONLY_PATTERN_WITH_NAME.matcher(text)
        while (testOnlyMatcher.find()) {
            val name = testOnlyMatcher.group(1)
            val startOffset = testOnlyMatcher.start()
            val endOffset = testOnlyMatcher.end()

            val element = psiFile.findElementAt(startOffset) ?: continue

            declarations.add(
                Declaration.Test(
                    name = name,
                    element = element,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    isExclusive = true
                )
            )
        }

        // Find regular tests (not .only)
        val testMatcher = TEST_PATTERN.matcher(text)
        while (testMatcher.find()) {
            val name = testMatcher.group(1)
            val startOffset = testMatcher.start()
            val endOffset = testMatcher.end()

            // Skip if this is already covered by test.only
            if (declarations.any { it.startOffset == startOffset && it is Declaration.Test }) {
                continue
            }

            val element = psiFile.findElementAt(startOffset) ?: continue

            declarations.add(
                Declaration.Test(
                    name = name,
                    element = element,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    isExclusive = false
                )
            )
        }

        return declarations
    }

    fun hasOnlyUsage(psiFile: PsiFile): Boolean {
        val text = psiFile.text
        return hasOnlyUsage(text)
    }

    fun hasOnlyUsage(text: String): Boolean {
        return FIXTURE_ONLY_PATTERN.matcher(text).find() ||
                TEST_ONLY_PATTERN.matcher(text).find()
    }

    fun getOnlyUsageDetails(psiFile: PsiFile): OnlyUsageInfo {
        val text = psiFile.text
        return getOnlyUsageDetails(text)
    }

    fun getOnlyUsageDetails(text: String): OnlyUsageInfo {
        val hasTestOnly = TEST_ONLY_PATTERN.matcher(text).find()
        val hasFixtureOnly = FIXTURE_ONLY_PATTERN.matcher(text).find()

        return OnlyUsageInfo(
            hasTestOnly = hasTestOnly,
            hasFixtureOnly = hasFixtureOnly,
            testOnlyCount = countMatches(TEST_ONLY_PATTERN, text),
            fixtureOnlyCount = countMatches(FIXTURE_ONLY_PATTERN, text)
        )
    }

    fun hasExclusiveDeclarations(psiFile: PsiFile): Boolean {
        return findTestCafeDeclarations(psiFile).any { it.isExclusive }
    }

    fun getExclusiveDeclarations(psiFile: PsiFile): List<Declaration> {
        return findTestCafeDeclarations(psiFile).filter { it.isExclusive }
    }

    private fun countMatches(pattern: Pattern, text: String): Int {
        val matcher = pattern.matcher(text)
        var count = 0
        while (matcher.find()) {
            count++
        }
        return count
    }

    private fun findPageAfterPosition(text: String, startPosition: Int): String? {
        val remainingText = text.substring(startPosition)
        val matcher = PAGE_PATTERN.matcher(remainingText)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    data class OnlyUsageInfo(
        val hasTestOnly: Boolean,
        val hasFixtureOnly: Boolean,
        val testOnlyCount: Int,
        val fixtureOnlyCount: Int
    ) {

        fun getDisplayMessage(): String {
            return "This file contains test.only() or fixture.only() - only these tests can be run!"
        }
    }
}
