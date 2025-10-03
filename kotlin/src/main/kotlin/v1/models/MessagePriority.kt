package v1.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MessagePriority(private val value: String) {
    HIGHEST("0"),
    HIGH("1"),
    MEDIUM("2"),
    LOW("3"),
    LOWEST("4");

    @JsonValue
    override fun toString(): String {
        return value
    }

    companion object {
        @JsonCreator
        fun fromValue(text: String?): MessagePriority {
            for (priority in MessagePriority.entries) {
                if (priority.value == text) {
                    return priority
                }
            }
            throw IllegalArgumentException("Unknown priority value: $text")
        }
    }
}
