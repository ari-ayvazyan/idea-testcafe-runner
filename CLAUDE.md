# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an IntelliJ IDEA plugin that provides seamless integration with TestCafe, allowing developers to run tests directly from the IDE with play buttons and customizable execution commands. The plugin is built using Kotlin/Java and the IntelliJ Platform SDK.

## Architecture

- **Language**: Kotlin with Java compatibility (JVM target 21)
- **Build System**: Gradle with IntelliJ Platform Gradle Plugin 2.7.1
- **Target IDE**: IntelliJ IDEA 2025.1.4.1+ (build 251+)
- **Plugin Structure**: Standard IntelliJ plugin layout with `plugin.xml` manifest

### Key Components
- `src/main/resources/META-INF/plugin.xml` - Plugin configuration and extension point declarations
- `testcafe/` - Sample TestCafe test project for development and testing
- Plugin will detect TestCafe files matching patterns: `*.spec.js`, `*.spec.ts`, `*.test.js`, `*.test.ts`, `*-test.js`, `*-test.ts`

## Development Commands

### Plugin Development
```bash
# Build the plugin
./gradlew build

# Run plugin in development IDE instance
./gradlew runIde

# Build plugin distribution
./gradlew buildPlugin

# Test the plugin
./gradlew test

# Clean build artifacts
./gradlew clean
```

### TestCafe Testing (for validation)
```bash
# Run TestCafe tests in sample project
cd testcafe && npm test

# Run headless TestCafe tests
cd testcafe && npm run test:headless
```

## Plugin Features to Implement

The plugin aims to provide:
1. **AST Analysis**: Parse TestCafe `fixture()` and `test()` declarations in JS/TS files
2. **UI Integration**: Add play buttons (▶️) next to test declarations in the editor
3. **Command Execution**: Run customizable TestCafe commands with placeholders like `{filePath}`, `{testName}`, `{fixtureName}`
4. **Results Display**: Show test output in IntelliJ's integrated Run tool window
5. **Context Menu**: Right-click integration for running tests
6. **Configuration**: Settings dialog for customizing TestCafe execution commands

## TestCafe Integration

Default command template: `npx testcafe chrome {filePath}`

Common TestCafe command patterns:
- Individual test: `npx testcafe chrome {filePath} -t "{testName}"`
- Fixture: `npx testcafe chrome {filePath} -f "{fixtureName}"`
- Headless: `npx testcafe chrome:headless {filePath}`
- Live mode: `npx testcafe chrome {filePath} --live`

## Development Notes

- Plugin targets JVM 21 and requires IntelliJ Platform 2025.1+
- TestCafe sample project is included in `testcafe/` for testing plugin functionality
- Plugin manifest is configured for basic platform dependency only
- Current version: 0.1.0 (development snapshot)