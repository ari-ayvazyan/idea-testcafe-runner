# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **TestCafe Support** plugin for IntelliJ IDEA that provides comprehensive TestCafe integration, including intelligent test execution, exclusive test handling (.only), and advanced editor features. The plugin is built using Kotlin/Java and the IntelliJ Platform SDK.

## Architecture

- **Language**: Kotlin with Java compatibility (JVM target 21)
- **Build System**: Gradle with IntelliJ Platform Gradle Plugin 2.7.1
- **Target IDE**: IntelliJ IDEA 2025.2.1+ (build 251-252.*)
- **Plugin Structure**: Standard IntelliJ plugin layout with `plugin.xml` manifest
- **Package Structure**: `at.itdo.tcrunner.*`

### Core Components Architecture

#### AST Analysis & Detection
- **`ASTAnalyzer.kt`**: Core regex-based parser that detects TestCafe `fixture()` and `test()` declarations, including `.only` variants
- **`Declaration.kt`**: Data classes representing TestCafe fixtures and tests with exclusivity metadata
- **`FileDetector.kt`**: Determines if files are TestCafe test files based on patterns

#### UI Integration
- **`LineMarkerProvider.kt`**: Adds play buttons (▶️) next to test declarations in the editor
- **`TestOnlyNotificationProvider.kt`**: Shows info bars when exclusive tests (.only) are detected
- **`SettingsConfigurable.kt`**: Plugin settings UI for command customization

#### Run System
- **`RunConfiguration.kt`**: Core run configuration for TestCafe execution
- **`RunConfigurationProducer.kt`**: Creates run configurations from context (right-click, etc.)
- **`ConfigurationType.kt`** + **`ConfigurationFactory.kt`**: IntelliJ run configuration type registration
- **`RunProfileState.kt`**: Manages actual test execution process
- **`CommandExecutor.kt`**: Executes TestCafe commands with placeholder substitution

#### Output Processing
- **`TestCafeOutputParser.kt`**: Parses TestCafe output and converts to IntelliJ test events
- **`TeamCityServiceMessageFormatter.kt`**: Formats test results for IntelliJ's test runner UI
- **`ProcessOutputHandler.kt`**: Handles real-time output streaming from TestCafe processes

#### Actions
- **`RunAllTestsInProjectAction.kt`**: Action to run all TestCafe tests in project
- **`RunAllTestsInDirectoryAction.kt`**: Action to run all tests in selected directory
- **`TestBaseAction.kt`**: Base class for TestCafe actions

### Key Features Implementation
- **Exclusive Test Detection**: Uses regex patterns to detect `.only` usage and shows appropriate UI warnings
- **Command Template System**: Supports placeholders like `{filePath}`, `{testName}`, `{fixtureName}`
- **Multi-Format Support**: Detects patterns: `*.spec.js`, `*.spec.ts`, `*.test.js`, `*.test.ts`, `*-test.js`, `*-test.ts`

## Development Commands

### Plugin Development
```bash
# Build the plugin
./gradlew build

# Run plugin in development IDE instance
./gradlew runIde

# Build plugin distribution
./gradlew buildPlugin

# Test the plugin (JUnit 5)
./gradlew test

# Run plugin verification
./gradlew verifyPlugin

# Publish plugin (requires PUBLISH_TOKEN env var)
./gradlew publishPlugin

# Clean build artifacts
./gradlew clean
```

### TestCafe Testing (for validation)
```bash
# Run TestCafe tests in sample project
cd testcafe && npm test

# Run headless TestCafe tests
cd testcafe && npm run test:headless

# Install TestCafe dependencies
cd testcafe && npm install
```

## TestCafe Integration

Default command template: `npx testcafe chrome {filePath}`

Common TestCafe command patterns:
- Individual test: `npx testcafe chrome {filePath} -t "{testName}"`
- Fixture: `npx testcafe chrome {filePath} -f "{fixtureName}"`
- Headless: `npx testcafe chrome:headless {filePath}`
- Live mode: `npx testcafe chrome {filePath} --live`

## Key Technical Details

### Testing Framework
- **Test Runner**: JUnit 5 with MockK for mocking
- **Test Structure**: Unit tests in `src/test/kotlin/`
- **Dependencies**: MockK 1.13.8, JUnit Jupiter 5.10.1, Kotlin Test JUnit5

### Extension Points Used
- **Line Marker Provider**: `com.intellij.codeInsight.lineMarkerProvider` for play buttons
- **Project Configurable**: Settings dialog integration under Tools menu
- **Configuration Type**: Custom run configuration type for TestCafe
- **Run Configuration Producer**: Context-aware run configuration creation
- **Editor Notification Provider**: Info bars for exclusive test warnings

### Plugin Manifest Configuration
- **Plugin ID**: `at.itdo.idea-testcafe-runner`
- **Dependencies**: Platform + JavaScript plugin (for AST parsing)
- **Actions**: Context menu and toolbar actions with keyboard shortcuts
- **Compatibility**: IntelliJ IDEA 251-252.* (2025.2.1+)

### Development Environment
- **IDE Target**: IntelliJ Ultimate 2025.2.1
- **Kotlin Version**: 2.1.0
- **Gradle Plugin**: IntelliJ Platform 2.7.1
- **JVM Target**: 21
- **Current Version**: 0.1.1

### Regex Patterns for AST Analysis
The plugin uses sophisticated regex patterns to detect TestCafe declarations:
- Regular fixtures: `(?<!\\.)fixture(?!\\.only)\\s*\\(\\s*['\"]([^'\"]*)['\"]`
- Exclusive fixtures: `fixture\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]`
- Regular tests: `(?<!\\.)test(?!\\.only)\\s*\\(\\s*['\"]([^'\"]*)['\"]`
- Exclusive tests: `test\\.only\\s*\\(\\s*['\"]([^'\"]*)['\"]`