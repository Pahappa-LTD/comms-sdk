package com.pahappa.systems.commssdk.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ApiResponseCode {
    OK, Failed;

    @JsonValue
    public String toJson() {
        return name();
    }

    @JsonCreator
    public ApiResponseCode fromJson(String json) {
        for (ApiResponseCode code : ApiResponseCode.values()) {
            if (code.name().equalsIgnoreCase(json)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + json);
    }
}
