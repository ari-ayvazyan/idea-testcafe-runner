package at.itdo.tcrunner

import org.junit.jupiter.api.Test

class UnicodeTest {
    @Test
    fun `debug Unicode characters`() {
        val ubuntu_fail = " ✖️ Test"
        val ubuntu_success = " ✓ Test"
        val windows_fail = " × Simple test 3 with err"
        val windows_success = " √ Simple test with console log"

        println("Ubuntu fail: '${ubuntu_fail[1]}'")
        println("Ubuntu fail length: ${ubuntu_fail.substring(1, 3).length}")
        println("Ubuntu fail codepoints: ${ubuntu_fail.substring(1, 3).codePoints().toArray().joinToString { "U+${it.toString(16).uppercase()}" }}")

        println("Ubuntu success: '${ubuntu_success[1]}'")
        println("Ubuntu success codepoints: ${ubuntu_success.substring(1, 2).codePoints().toArray().joinToString { "U+${it.toString(16).uppercase()}" }}")

        println("Windows fail: '${windows_fail[1]}'")
        println("Windows fail codepoints: ${windows_fail.substring(1, 2).codePoints().toArray().joinToString { "U+${it.toString(16).uppercase()}" }}")

        println("Windows success: '${windows_success[1]}'")
        println("Windows success codepoints: ${windows_success.substring(1, 2).codePoints().toArray().joinToString { "U+${it.toString(16).uppercase()}" }}")

        // Test different patterns
        val pattern1 = Regex(" [√×✖️✓] .*")
        val pattern2 = Regex(" [√×✖✓] .*")
        val pattern3 = Regex(" (?:√|×|✖️|✓) .*")
        val pattern4 = Regex(" (?:√|×|✖|✓).*? .*")

        println("\nPattern testing:")
        listOf(ubuntu_fail, ubuntu_success, windows_fail, windows_success).forEach { test ->
            println("'$test'")
            println("  Pattern 1 [√×✖️✓]: ${test.matches(pattern1)}")
            println("  Pattern 2 [√×✖✓]: ${test.matches(pattern2)}")
            println("  Pattern 3 (?:√|×|✖️|✓): ${test.matches(pattern3)}")
            println("  Pattern 4 (?:√|×|✖|✓).*?: ${test.matches(pattern4)}")
            println()
        }
    }
}
