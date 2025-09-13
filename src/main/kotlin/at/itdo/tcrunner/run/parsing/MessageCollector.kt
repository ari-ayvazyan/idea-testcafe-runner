package at.itdo.tcrunner.run.parsing

class MessageCollector {
    private val messages: MutableList<String> = mutableListOf()

    fun addMessage(message: String) {
        if (message.isNotBlank()) {
            messages.add(message)
        }
    }

    fun getCollectedMessages(): String {
        return if (messages.isNotEmpty()) {
            messages.joinToString("\n")
        } else {
            ""
        }
    }

    fun getCollectedMessagesOrDefault(defaultMessage: String): String {
        return if (messages.isNotEmpty()) {
            messages.joinToString("\n")
        } else {
            defaultMessage
        }
    }

    fun clear() {
        messages.clear()
    }

    fun hasMessages(): Boolean = messages.isNotEmpty()
}