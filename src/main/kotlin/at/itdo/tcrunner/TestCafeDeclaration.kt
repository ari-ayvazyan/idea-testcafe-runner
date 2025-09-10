package at.itdo.tcrunner

import com.intellij.psi.PsiElement

sealed class TestCafeDeclaration(
    val name: String,
    val element: PsiElement,
    val startOffset: Int,
    val endOffset: Int
) {

    class Fixture(
        name: String,
        element: PsiElement,
        startOffset: Int,
        endOffset: Int,
        val page: String? = null
    ) : TestCafeDeclaration(name, element, startOffset, endOffset)

    class Test(
        name: String,
        element: PsiElement,
        startOffset: Int,
        endOffset: Int,
        val fixture: String? = null
    ) : TestCafeDeclaration(name, element, startOffset, endOffset)
}
