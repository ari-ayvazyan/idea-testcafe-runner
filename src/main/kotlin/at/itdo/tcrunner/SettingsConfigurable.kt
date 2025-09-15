package at.itdo.tcrunner

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class SettingsConfigurable(private val project: Project) : Configurable {

    private val settings = Settings.getInstance(project)

    // UI Components
    private val defaultCommandField = JBTextField()
    private val testCommandField = JBTextField()
    private val fixtureCommandField = JBTextField()
    private val browserField = JBTextField()
    private val headlessModeBox = JBCheckBox("Run in headless mode")
    private val liveModeBox = JBCheckBox("Enable live mode")
    private val concurrencySpinner = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val timeoutSpinner = JSpinner(SpinnerNumberModel(30000, 1000, 300000, 1000))
    private val filePatternsField = JBTextField()
    private val workingDirectoryField = JBTextField()

    override fun getDisplayName(): String = "TestCafe Runner"

    override fun createComponent(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Default command:"), defaultCommandField, 1, false)
            .addTooltip("Command template for running entire files. Use {filePath} placeholder.")
            .addLabeledComponent(JBLabel("Test command:"), testCommandField, 1, false)
            .addTooltip("Command template for running individual tests. Use {filePath} and {testName} placeholders.")
            .addLabeledComponent(JBLabel("Fixture command:"), fixtureCommandField, 1, false)
            .addTooltip("Command template for running fixtures. Use {filePath} and {fixtureName} placeholders.")
            .addSeparator()
            .addLabeledComponent(JBLabel("Browser:"), browserField, 1, false)
            .addTooltip("Default browser for TestCafe (e.g., chrome, firefox, safari)")
            .addComponent(headlessModeBox, 1)
            .addComponent(liveModeBox, 1)
            .addLabeledComponent(JBLabel("Concurrency:"), concurrencySpinner, 1, false)
            .addTooltip("Number of browser instances to run in parallel")
            .addLabeledComponent(JBLabel("Timeout (ms):"), timeoutSpinner, 1, false)
            .addTooltip("Test execution timeout in milliseconds")
            .addSeparator()
            .addLabeledComponent(JBLabel("File regex patterns:"), filePatternsField, 1, false)
            .addTooltip("Comma-separated regex patterns to detect TestCafe files (e.g., .*\\.spec\\.(js|ts)$,.*\\.test\\.(js|ts)$)")
            .addSeparator()
            .addLabeledComponent(JBLabel("Working directory:"), workingDirectoryField, 1, false)
            .addTooltip("Default working directory for TestCafe commands (leave empty to detect automatically)")
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        return defaultCommandField.text != settings.defaultCommand ||
               testCommandField.text != settings.testCommand ||
               fixtureCommandField.text != settings.fixtureCommand ||
               browserField.text != settings.browser ||
               headlessModeBox.isSelected != settings.headlessMode ||
               liveModeBox.isSelected != settings.liveMode ||
               concurrencySpinner.value != settings.concurrency ||
               timeoutSpinner.value != settings.timeout ||
               filePatternsField.text != settings.filePatterns ||
               workingDirectoryField.text != settings.workingDirectory
    }

    override fun apply() {
        // Validate command templates before applying
        validateCommandTemplates()

        settings.defaultCommand = defaultCommandField.text
        settings.testCommand = testCommandField.text
        settings.fixtureCommand = fixtureCommandField.text
        settings.browser = browserField.text
        settings.headlessMode = headlessModeBox.isSelected
        settings.liveMode = liveModeBox.isSelected
        settings.concurrency = concurrencySpinner.value as Int
        settings.timeout = timeoutSpinner.value as Int
        settings.filePatterns = filePatternsField.text
        settings.workingDirectory = workingDirectoryField.text
    }

    private fun validateCommandTemplates() {
        // Validate default command contains required placeholder
        if (!defaultCommandField.text.contains("{filePath}")) {
            throw ConfigurationException("Default command must contain {filePath} placeholder", "Invalid Default Command")
        }

        // Validate test command contains required placeholders
        if (!testCommandField.text.contains("{filePath}") || !testCommandField.text.contains("{testName}")) {
            throw ConfigurationException("Test command must contain both {filePath} and {testName} placeholders", "Invalid Test Command")
        }

        // Validate fixture command contains required placeholders
        if (!fixtureCommandField.text.contains("{filePath}") || !fixtureCommandField.text.contains("{fixtureName}")) {
            throw ConfigurationException("Fixture command must contain both {filePath} and {fixtureName} placeholders", "Invalid Fixture Command")
        }

        // Validate browser field is not empty
        if (browserField.text.isBlank()) {
            throw ConfigurationException("Browser field cannot be empty", "Invalid Browser Configuration")
        }

        // Validate commands are not empty
        if (defaultCommandField.text.isBlank()) {
            throw ConfigurationException("Default command cannot be empty", "Invalid Default Command")
        }

        if (testCommandField.text.isBlank()) {
            throw ConfigurationException("Test command cannot be empty", "Invalid Test Command")
        }

        if (fixtureCommandField.text.isBlank()) {
            throw ConfigurationException("Fixture command cannot be empty", "Invalid Fixture Command")
        }

        // Validate file patterns
        if (filePatternsField.text.isBlank()) {
            throw ConfigurationException("File patterns cannot be empty", "Invalid File Patterns")
        }

        // Validate file patterns format and regex validity
        val patterns = filePatternsField.text.split(",").map { it.trim() }
        if (patterns.any { it.isBlank() }) {
            throw ConfigurationException("File patterns cannot contain empty entries", "Invalid File Patterns")
        }

        // Validate each pattern is a valid regex
        patterns.forEach { pattern ->
            try {
                Regex(pattern)
            } catch (e: Exception) {
                throw ConfigurationException("Invalid regex pattern: '$pattern'. ${e.message}", "Invalid Regex Pattern")
            }
        }
    }

    override fun reset() {
        defaultCommandField.text = settings.defaultCommand
        testCommandField.text = settings.testCommand
        fixtureCommandField.text = settings.fixtureCommand
        browserField.text = settings.browser
        headlessModeBox.isSelected = settings.headlessMode
        liveModeBox.isSelected = settings.liveMode
        concurrencySpinner.value = settings.concurrency
        timeoutSpinner.value = settings.timeout
        filePatternsField.text = settings.filePatterns
        workingDirectoryField.text = settings.workingDirectory
    }

}
