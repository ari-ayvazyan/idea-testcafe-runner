package at.itdo.tcrunner.run

import at.itdo.tcrunner.TestCafeSettings
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TestCafeRunConfigurationEditor : SettingsEditor<TestCafeRunConfiguration>() {

    private val scriptPathField = TextFieldWithBrowseButton()
    private val testFilterField = JBTextField()
    private val fixtureFilterField = JBTextField()
    private val browserField = JBTextField()
    private val headlessModeBox = JBCheckBox("Headless mode")
    private val liveModeBox = JBCheckBox("Live mode")
    private val customCommandField = JBTextField()
    private val workingDirectoryField = TextFieldWithBrowseButton()
    
    private var currentConfiguration: TestCafeRunConfiguration? = null

    override fun createEditor(): JComponent {
        // Setup file chooser for script path
        scriptPathField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFileDescriptor("js")
                    .withTitle("Select TestCafe Script")
                    .withDescription("Choose TestCafe test file")
            )
        )

        // Setup directory chooser for working directory
        workingDirectoryField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withTitle("Select Working Directory")
                    .withDescription("Choose working directory for TestCafe execution")
            )
        )

        // Set default browser
        browserField.text = "chrome"
        
        // Add listeners to update custom command field when filters change
        setupFieldListeners()

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Script path:", scriptPathField)
            .addLabeledComponent("Test filter:", testFilterField)
            .addLabeledComponent("Fixture filter:", fixtureFilterField)
            .addLabeledComponent("Browser:", browserField)
            .addComponent(headlessModeBox)
            .addComponent(liveModeBox)
            .addLabeledComponent("Custom command:", customCommandField)
            .addLabeledComponent("Working directory:", workingDirectoryField)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun setupFieldListeners() {
        val documentListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updateCustomCommandFieldIfEmpty()
            override fun removeUpdate(e: DocumentEvent?) = updateCustomCommandFieldIfEmpty()
            override fun changedUpdate(e: DocumentEvent?) = updateCustomCommandFieldIfEmpty()
        }
        
        testFilterField.document.addDocumentListener(documentListener)
        fixtureFilterField.document.addDocumentListener(documentListener)
    }
    
    private fun updateCustomCommandFieldIfEmpty() {
        // Only update if custom command field is empty
        if (customCommandField.text.isBlank()) {
            updateCustomCommandField()
        }
    }
    
    private fun updateCustomCommandField() {
        val configuration = currentConfiguration ?: return
        val settings = TestCafeSettings.getInstance(configuration.project)
        
        val template = when {
            testFilterField.text.isNotBlank() -> settings.getCommandTemplate(TestCafeSettings.CommandType.TEST)
            fixtureFilterField.text.isNotBlank() -> settings.getCommandTemplate(TestCafeSettings.CommandType.FIXTURE)
            else -> settings.getCommandTemplate(TestCafeSettings.CommandType.FILE)
        }
        
        customCommandField.text = template
    }

    override fun resetEditorFrom(configuration: TestCafeRunConfiguration) {
        currentConfiguration = configuration
        
        scriptPathField.text = configuration.getScriptPath()
        testFilterField.text = configuration.getTestFilter()
        fixtureFilterField.text = configuration.getFixtureFilter()
        browserField.text = configuration.getBrowser()
        headlessModeBox.isSelected = configuration.getHeadlessMode()
        liveModeBox.isSelected = configuration.getLiveMode()
        customCommandField.text = configuration.getCustomCommand()
        workingDirectoryField.text = configuration.getWorkingDirectory()
        
        // Populate custom command field with template if it's empty
        if (configuration.getCustomCommand().isBlank()) {
            updateCustomCommandField()
        }
    }

    override fun applyEditorTo(configuration: TestCafeRunConfiguration) {
        configuration.setScriptPath(scriptPathField.text)
        configuration.setTestFilter(testFilterField.text)
        configuration.setFixtureFilter(fixtureFilterField.text)
        configuration.setBrowser(browserField.text)
        configuration.setHeadlessMode(headlessModeBox.isSelected)
        configuration.setLiveMode(liveModeBox.isSelected)
        configuration.setCustomCommand(customCommandField.text)
        configuration.setWorkingDirectory(workingDirectoryField.text)
    }
}
