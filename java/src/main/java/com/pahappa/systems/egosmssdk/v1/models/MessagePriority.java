package com.pahappa.systems.egosmssdk.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MessagePriority {
    HIGHEST("0"),
    HIGH("1"),
    MEDIUM("2"),
    LOW("3"),
    LOWEST("4");

    private final String value;

    MessagePriority(String value) {
        this.value = value;
    }

    @Override @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static MessagePriority fromValue(String text) {
        for (MessagePriority priority : MessagePriority.values()) {
            if (priority.value.equals(text)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority value: " + text);
    }
}
