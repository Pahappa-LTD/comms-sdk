package v1.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ApiResponseCode {
    OK, Failed;

    @JsonValue
    fun toJson(): String {
        return name
    }

    @JsonCreator
    fun fromJson(json: String?): ApiResponseCode {
        for (code in ApiResponseCode.entries) {
            if (code.name.equals(json, ignoreCase = true)) {
                return code
            }
        }
        throw IllegalArgumentException("Unknown value: $json")
    }
}
