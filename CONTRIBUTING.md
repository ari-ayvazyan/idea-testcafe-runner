# Contributing to TestCafe Runner

Thank you for your interest in contributing to the TestCafe Runner plugin! This document provides guidelines and information for contributors.

## Getting Started

### Prerequisites
- IntelliJ IDEA 2025.1 or later (Community or Ultimate)
- Java 21 or later
- Gradle (included via wrapper)
- Basic knowledge of Kotlin/Java and IntelliJ Plugin Development

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourrepo/idea-testcafe-runner.git
   cd idea-testcafe-runner
   ```

2. **Import into IntelliJ IDEA**
   - Open IntelliJ IDEA
   - Select "Open" and choose the project directory
   - Wait for Gradle sync to complete

3. **Run the plugin in development**
   ```bash
   ./gradlew runIde
   ```
   This starts a new IntelliJ instance with the plugin loaded.

4. **Build the plugin**
   ```bash
   ./gradlew buildPlugin
   ```
   The built plugin will be in `build/distributions/`

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions focused and small
- Use IntelliJ's code formatting (Ctrl+Alt+L)

### Architecture Principles
- Use proper extension points and services
- Implement proper disposal for resources
- Handle exceptions gracefully
- Use background tasks for long-running operations

## Contributing Process

### Reporting Issues
1. Check existing issues first to avoid duplicates
2. Use the issue template when creating new issues
3. Provide clear reproduction steps
4. Include relevant system information (OS, IntelliJ version, plugin version)

### Submitting Changes

1. **Fork the repository** on GitHub

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Write tests for new functionality
   - Ensure existing tests pass
   - Follow the coding guidelines

4. **Test thoroughly**
   ```bash
   # Run tests
   ./gradlew test
   
   # Run plugin in development mode
   ./gradlew runIde
   
   # Verify plugin builds correctly
   ./gradlew buildPlugin
   ```

5. **Commit your changes**
   - Use clear, descriptive commit messages
   - Follow conventional commit format if possible:
     ```
     feat: add support for TypeScript test files
     fix: resolve issue with fixture name parsing
     docs: update installation instructions
     ```

6. **Push and create a Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```
   - Provide a clear description of the changes
   - Reference any related issues
   - Include screenshots for UI changes

### Pull Request Guidelines
- Keep PRs focused on a single feature/fix
- Write clear titles and descriptions
- Include tests for new functionality
- Update documentation if needed
- Ensure CI checks pass

## Testing

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest
```

### Test Structure
- Unit tests in `src/test/kotlin/`
- Integration tests in `src/integrationTest/kotlin/`
- Test data in `src/test/testData/`

### Writing Tests
- Test both positive and negative cases
- Mock external dependencies appropriately
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)

## Plugin Development Resources

### IntelliJ Platform Documentation
- [Plugin Development Guidelines](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html)
- [Extension Points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html)

### Useful Tools
- [Plugin DevKit](https://plugins.jetbrains.com/docs/intellij/using-dev-kit.html)
- [PSI Viewer](https://plugins.jetbrains.com/docs/intellij/explore-api.html#31-psi-viewer)
- [Internal Actions](https://plugins.jetbrains.com/docs/intellij/internal-actions-intro.html)

## Feature Requests

### Proposing New Features
1. Open an issue with the "enhancement" label
2. Describe the problem you're solving
3. Propose a solution with examples
4. Discuss implementation approaches
