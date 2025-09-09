# TestCafe Runner for IntelliJ IDEA

A powerful IntelliJ IDEA plugin that provides seamless integration with TestCafe, allowing you to run tests directly from your IDE with intuitive play buttons and customizable execution commands.

## Features

- **Smart Test Detection**: Automatically detects TestCafe fixtures and tests in `.spec.js`, `.spec.ts`, and other test files
- **One-Click Execution**: Run individual tests or entire fixtures with convenient play buttons in the editor
- **Flexible Configuration**: Customize TestCafe commands and parameters through the plugin settings
- **Integrated Output**: View test results directly in IntelliJ's run tool window
- **Context Menu Integration**: Right-click to run tests from the context menu
- **Multiple File Format Support**: Works with JavaScript, TypeScript, and ES6 TestCafe tests

## Installation

### From JetBrains Marketplace
1. Open IntelliJ IDEA
2. Go to `File → Settings → Plugins` (or `IntelliJ IDEA → Preferences → Plugins` on macOS)
3. Click the `Marketplace` tab
4. Search for "TestCafe Runner"
5. Click `Install` and restart the IDE

### Manual Installation
1. Download the plugin JAR file from the [releases page](https://github.com/yourrepo/idea-testcafe-runner/releases)
2. Go to `File → Settings → Plugins`
3. Click the gear icon and select `Install Plugin from Disk...`
4. Select the downloaded JAR file
5. Restart IntelliJ IDEA

## Quick Start

1. **Open a TestCafe project** in IntelliJ IDEA
2. **Navigate to a test file** (e.g., `tests/login.spec.js`)
3. **Look for play buttons** (▶️) that appear next to your `fixture()` and `test()` declarations
4. **Click a play button** to run the corresponding test or fixture

## Configuration

### Basic Setup

1. Go to `File → Settings → Tools → TestCafe Runner` (or `IntelliJ IDEA → Preferences → Tools → TestCafe Runner` on macOS)
2. Configure your TestCafe command template (default: `npx testcafe chrome {filePath}`)
3. Adjust parameters as needed for your project setup

### Command Templates

The plugin supports flexible command templates with the following placeholders:

- `{filePath}` - The path to the test file
- `{testName}` - The name of the specific test (when running individual tests)
- `{fixtureName}` - The name of the fixture (when running fixture-level tests)

#### Example Configurations

**Basic Chrome execution:**
```
npx testcafe chrome {filePath}
```

**With specific test filtering:**
```
npx testcafe chrome {filePath} -t "{testName}"
```

**With fixture filtering:**
```
npx testcafe chrome {filePath} -f "{fixtureName}"
```

**Custom browser and options:**
```
npx testcafe "chrome --incognito" {filePath} --live
```

**Docker-based execution:**
```
docker run -v $(pwd):/tests testcafe/testcafe chrome /tests/{filePath}
```

### Advanced Settings

- **Test File Patterns**: Customize which files are recognized as TestCafe tests
- **Browser Configuration**: Set default browsers for test execution
- **Output Formatting**: Configure how test results are displayed
- **Parallel Execution**: Enable running tests in parallel

## Usage

### Running Tests

#### Individual Tests
1. Open a TestCafe test file
2. Find the `test()` function you want to run
3. Click the ▶️ play button next to it
4. View results in the Run tool window

#### Entire Fixtures
1. Locate a `fixture()` declaration
2. Click the ▶️ play button next to it
3. All tests in that fixture will execute

#### Context Menu
1. Right-click on any `test()` or `fixture()` line
2. Select "Run TestCafe Test" or "Run TestCafe Fixture"

### Viewing Results

Test results appear in IntelliJ's integrated Run tool window, showing:
- Test execution status (passed/failed)
- Detailed error messages and stack traces
- Execution time and performance metrics
- Screenshots and videos (if configured in TestCafe)

## Supported File Types

The plugin automatically detects TestCafe tests in files matching these patterns:
- `*.spec.js`
- `*.spec.ts`
- `*.test.js`
- `*.test.ts`
- `*-test.js`
- `*-test.ts`

## Requirements

- IntelliJ IDEA 2025.1+ or other JetBrains IDEs
- TestCafe installed in your project (npm package)
- Node.js runtime environment

## Troubleshooting

### Common Issues

**Play buttons don't appear**
- Ensure your test files match the supported file patterns
- Verify that TestCafe syntax is correctly used (`fixture()` and `test()` functions)
- Check that the file is recognized as JavaScript/TypeScript

**Tests fail to execute**
- Verify your TestCafe command template in settings
- Ensure TestCafe is properly installed (`npm install testcafe`)
- Check that the specified browser is available

**Output not displaying**
- Confirm the Run tool window is visible (`View → Tool Windows → Run`)
- Check IntelliJ console for any error messages

### Getting Help

- Check the [FAQ section](#faq)
- Report issues on [GitHub](https://github.com/yourrepo/idea-testcafe-runner/issues)
- Contact support through the JetBrains Marketplace plugin page

## FAQ

**Q: Can I use different browsers for different tests?**
A: Yes, configure multiple command templates or modify the browser parameter in your TestCafe commands.

**Q: Can I run tests on remote browsers or devices?**
A: Yes, configure your command template to use TestCafe's remote browser or device testing features.

**Q: Is debugging supported?**
A: Currently, the plugin focuses on test execution. For debugging, use TestCafe's built-in debugging capabilities or run tests with the `--debug-on-fail` option.

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on how to:
- Report bugs
- Suggest features
- Submit pull requests
- Set up the development environment


## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed history of changes and updates.