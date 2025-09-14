package at.itdo.tcrunner

import com.intellij.psi.PsiElement

sealed class Declaration(
    val name: String,
    val element: PsiElement,
    val startOffset: Int,
    val endOffset: Int,
    val isExclusive: Boolean = false
) {

    class Fixture(
        name: String,
        element: PsiElement,
        startOffset: Int,
        endOffset: Int,
        val page: String? = null,
        isExclusive: Boolean = false
    ) : Declaration(name, element, startOffset, endOffset, isExclusive)

    class Test(
        name: String,
        element: PsiElement,
        startOffset: Int,
        endOffset: Int,
        val fixture: String? = null,
        isExclusive: Boolean = false
    ) : Declaration(name, element, startOffset, endOffset, isExclusive)
}
