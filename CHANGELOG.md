# Changelog

All notable changes to the TestCafe Runner plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned Features
- Test result parsing and rich display with pass/fail indicators
- Test explorer/tree view for project-wide test navigation
- Test history and favorites functionality
- Integration with version control for test annotations
- Support for parallel test execution
- Advanced filtering and search capabilities in test results

## [0.1.0] - 2025-09-08

### Added
- Initial release of TestCafe Runner plugin
- Smart detection of TestCafe fixtures and tests in JavaScript/TypeScript files
- Automatic recognition of `.spec.js`, `.spec.ts`, `.test.js`, `.test.ts` files
- Play buttons (▶️) next to `fixture()` and `test()` declarations for one-click execution
- Configurable TestCafe command templates with placeholder support
- Integration with IntelliJ's run tool window for test output
- Context menu integration for running tests via right-click
- Support for both individual test and entire fixture execution
- Command template placeholders: `{filePath}`, `{testName}`, `{fixtureName}`
- Basic error handling for test execution failures
- Plugin settings dialog for command customization

### Technical Details
- Built for IntelliJ IDEA 2025.1+
- Supports Java/Kotlin development with JVM target 21
- Uses IntelliJ Platform SDK 2025.1.4.1
- Plugin ID: `at.itdo.idea-testcafe-runner`

### Requirements
- IntelliJ IDEA 2025.1 or later
- TestCafe installed in your project
- Node.js runtime environment

---

## Release Notes Format

### Version Schema
- **Major.Minor.Patch** (e.g., 1.2.3)
- **Major**: Breaking changes or significant new functionality
- **Minor**: New features, backward compatible
- **Patch**: Bug fixes, minor improvements

### Change Categories
- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security improvements

### Publishing Process
1. Update version in `build.gradle.kts`
2. Update changelog with new version details
3. Update `changeNotes` in `build.gradle.kts` with latest changes
4. Create git tag with version number
5. Build and publish to JetBrains Marketplace using `./gradlew publishPlugin`