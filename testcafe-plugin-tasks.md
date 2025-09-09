# TestCafe IntelliJ Plugin Development Tasks

## Project Overview
Develop an IntelliJ plugin that detects TestCafe fixtures and tests in source files, displays play buttons for execution, and allows custom command configuration.

**🎯 MAJOR ACCOMPLISHMENT: Fixed critical duplicate play button issue!**

### 0. Documentation & Packaging
- [x] Write user documentation
- [x] Prepare for JetBrains marketplace submission
- [x] Create plugin changelog and version management
## Core Features

### 1. File Analysis & Detection
- [x] Implement TestCafe file parser to detect `.spec.js`, `.spec.ts`, test files
- [x] Create AST analyzer to identify `fixture()` declarations, investigate https://testcafe.io/documentation/ for formatting
- [x] Create AST analyzer to identify `test()` declarations  
- [x] Handle different TestCafe syntax variations (ES6, TypeScript, Javascript)
- [x] Add play buttons next to fixture declarations
- [x] Add play buttons next to test declarations
- [x] Fix duplicate play button issue (removed duplicate line marker registrations)
- [x] Implement right-click context menu integration

### 3. Command Execution
- [x] Implement command runner service
- [x] Build dynamic command construction with file path parameter
- [x] Add `-t` (test) filter parameter support
- [x] Add `-f` (fixture) filter parameter support
- [x] Handle command output parsing and display, display it as an intellij test run window
- [x] Add error handling for failed test executions

### 4. Configuration System
- [x] Design plugin settings UI/dialog
- [x] Create configuration model for custom TestCafe commands
- [x] Implement settings persistence to `.idea` folder
- [x] Add validation for command templates
- [x] Create default command templates for common setups
- [x] Make it possible to configure file matches for test files (e.g. `*.test.js`, `*.spec.ts`)
- [x] ensure that configurations are project-specific, stored in the .idea folder and do not affect global IDE settings

### 5. IDE Integration
- [x] Create plugin manifest (`plugin.xml`)
- [x] Register file type associations
- [x] Implement extension points for editor integration
- [x] Add toolbar actions for bulk test operations
- [x] Run all tests in project action
- [x] Run all tests in current directory action  
- [x] Configure TestCafe settings shortcut action
- [x] Integrate with IntelliJ's run configuration system

### 6. Advanced Features
- [ ] Implement test result parsing and display
- [ ] Create test explorer/tree view
- [ ] Add filtering and search capabilities
- [ ] Implement test history and favorites
- [ ] Add support for parallel test execution
- [ ] Create integration with version control (git annotations)
