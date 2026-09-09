# Discovered Issues & Compatibility Analysis

This document summarizes the issues identified in the TestCafe Support plugin when running on non-developer machines, different operating systems (Linux, macOS, Windows), or varying environments.

---

### 1. Cross-Platform Execution & Shell Environment Issues (`RunProfileState.kt`)

* **Interactive Login Shell Usage (`bash -l -i -c`)**:
  On non-Windows systems, `RunProfileState` launched TestCafe via `bash -l -i -c "echo \"Working directory: \$(pwd)\" && $command"`.
  * **Interactive Shell (`-i`) Failure**: An interactive shell requires a TTY. In CI environments, Docker containers, or background IDE execution without a pseudo-terminal, `bash -i` fails or emits warnings/errors (e.g., `bash: cannot set terminal process group`, `no job control in this shell`).
  * **Shell Availability**: Hardcoding `bash` fails on minimal systems or Docker images (e.g., Alpine Linux, minimal containers) where `sh` or `zsh` is default and `bash` is not installed or not in PATH.
  * **Stdout Pollution**: Printing `Working directory: ...` directly to standard output pollutes the stream and interferes with initial TestCafe output parsing.

* **Windows Execution Wrappers**:
  On Windows, `powershell -Command "Write-Host ...; $command"` was used. Powershell execution policy or missing powershell binaries in custom environments can fail test execution.

---

### 2. Output Parsing & Line Classification Bugs (`TestCafeLineClassifier.kt`, `TestCafeOutputParser.kt`)

* **Unstripped ANSI Escape Sequences**:
  When TestCafe outputs colored terminal text (standard when running in interactive terminals or default settings), lines contain ANSI color escape sequences (e.g., `\u001b[31m`, `\u001b[0m`).
  Because `TestCafeLineClassifier` did not strip ANSI escape sequences before running regular expressions, lines like `\u001b[31m × Simple test 3 with err\u001b[0m` failed to match result patterns (` Regex(" (?:√|×|✖️|✓) .*)`), leading to test results being misclassified as `REGULAR_MESSAGE` or `FIXTURE`.

* **Missing / Incomplete Test Status Symbol Matching**:
  TestCafe uses different Unicode characters for pass/fail status depending on platform, Node version, terminal capabilities, and TestCafe version:
  * Checkmark variants: `√` (`\u221a`), `✓` (`\u2713`)
  * Cross/Fail variants: `×` (`\u00d7`), `✖` (`\u2716`), `✖️` (`\u2716\ufe0f`)
  `TestCafeLineClassifier` missed `✖` (`\u2716` without the variation selector `\ufe0f`), which is standard on Ubuntu and many Linux terminal environments.

* **Misclassification of Fixture Lines**:
  `isFixtureLine` classified any single-space-indented line not matching a test result as a fixture:
  ```kotlin
  return line.startsWith(" ") && !line.startsWith("  ") && !line.matches(Regex(" [√×] .*")) && trimmed != "--"
  ```
  Because `[√×]` only checked two symbols, failure lines using `✖` or `✖️` or `✓` were wrongly misclassified as `FIXTURE` starts, resetting fixture scope and corrupting the test tree.

* **Console Log Attribution & Error Message Leakage**:
  When a test failed, `collectingFailedTestMessages` was set to `true` to capture stack traces.
  Subsequent lines—including console logs emitted by the *next* test before its result symbol was printed—were incorrectly appended to the failed test's message buffer, leaving the subsequent test with missing log output.

---

### 3. Duration & Timing Parsing Incompatibilities (`TeamCityEventEmitter.kt`)

* **Unsupported Composite & Decimal Duration Formats**:
  `convertDurationToMs` supported only simple single-unit integer durations ending with `s`, `ms`, or `m` (e.g., `1s`, `500ms`).
  It failed on:
  * Decimal durations: `1.5s`, `0.2s`
  * Composite durations: `1m 30s`, `2m 5s`
  * Millisecond formats: `1s 200ms`
  Strings like `1m 30s` fell into the `else` branch and evaluated to `0`, causing test timing in the IntelliJ UI to report 0ms.

---

### 4. AST Analysis & Test File Detection (`FileDetector.kt`, `ASTAnalyzer.kt`)

* **Strict Relative / Absolute Path Variations**:
  TestCafe files opened from virtual or temporary directories, symlinked folders, or Windows drive letter variations (e.g. `d:\` vs `D:\`) could fail line marker matching or test location navigation (`TestLocator.kt`).

---

### Summary of Fixes Required
1. Strip ANSI escape sequences prior to line classification.
2. Expand Unicode symbol regexes to cover `√`, `✓`, `×`, `✖`, `✖️`.
3. Improve `isFixtureLine` and failure message collection boundaries.
4. Support decimal and composite duration strings (`1m 30s`, `1.5s`).
5. Generalize process execution in `RunProfileState.kt` to run commands safely without interactive flags or stdout pollution.
6. Expand unit test suite to prevent regressions across platforms.
