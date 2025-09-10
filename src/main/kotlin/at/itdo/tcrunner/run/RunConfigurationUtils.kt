package at.itdo.tcrunner.run

import at.itdo.tcrunner.Declaration
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.project.Project

object RunConfigurationUtils {

    fun executeWithTemporaryRunConfiguration(
        project: Project,
        declaration: Declaration,
        filePath: String
    ) {
        val runManager = RunManager.getInstance(project)
        val configurationType = ConfigurationType.INSTANCE
        val factory = configurationType.configurationFactories[0]

        // Create a temporary run configuration
        val runConfiguration = factory.createTemplateConfiguration(project) as RunConfiguration

        // Set configuration based on declaration type
        runConfiguration.setScriptPath(filePath)

        when (declaration) {
            is Declaration.Test -> {
                runConfiguration.setTestFilter(declaration.name)
                runConfiguration.name = "TestCafe Test: ${declaration.name}"
            }
            is Declaration.Fixture -> {
                runConfiguration.setFixtureFilter(declaration.name)
                runConfiguration.name = "TestCafe Fixture: ${declaration.name}"
            }
        }

        // Create a temporary run configuration settings object
        val settings = runManager.createConfiguration(runConfiguration, factory)
        settings.isTemporary = true

        // Add to run manager temporarily and set as selected
        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings

        // Create execution environment and execute
        val environment = ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), settings)
            .build()

        ProgramRunnerUtil.executeConfiguration(environment.runnerAndConfigurationSettings!!, environment.executor)
    }

    fun executeFileWithTemporaryRunConfiguration(
        project: Project,
        filePath: String,
        configurationName: String? = null
    ) {
        val runManager = RunManager.getInstance(project)
        val configurationType = ConfigurationType.INSTANCE
        val factory = configurationType.configurationFactories[0]

        // Create a temporary run configuration
        val runConfiguration = factory.createTemplateConfiguration(project) as RunConfiguration

        // Set configuration for entire file execution
        runConfiguration.setScriptPath(filePath)
        runConfiguration.name = configurationName ?: "TestCafe: ${filePath.substringAfterLast("/")}"

        // Create a temporary run configuration settings object
        val settings = runManager.createConfiguration(runConfiguration, factory)
        settings.isTemporary = true

        // Add to run manager temporarily and set as selected
        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings

        // Create execution environment and execute
        val environment = ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), settings)
            .build()

        ProgramRunnerUtil.executeConfiguration(environment.runnerAndConfigurationSettings!!, environment.executor)
    }
}
