package at.itdo.tcrunner.util

import at.itdo.tcrunner.Settings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

object TCLogger {
    private val LOG = Logger.getInstance("#" + TCLogger::class.java.getPackage().name)

    fun info(message: String) {
        LOG.info(message)
    }

    fun debug(project: Project, message: String) {
        if (Settings.getInstance(project).debugMode) {
            LOG.info("[DEBUG] $message")
        }
    }

    fun warn(message: String) {
        LOG.warn(message)
    }

    fun error(message: String, t: Throwable? = null) {
        LOG.error(message, t)
    }
}
