package v1.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class WalletType(private val value: String) {
    LOCAL("Local"),
    INTERNATIONAL("International");

    @JsonValue
    override fun toString(): String {
        return value
    }

    companion object {
        @JsonCreator
        fun fromValue(text: String?): WalletType {
            for (type in WalletType.entries) {
                if (type.value == text) {
                    return type
                }
            }
            throw IllegalArgumentException("Unknown wallet type value: $text")
        }
    }
}
