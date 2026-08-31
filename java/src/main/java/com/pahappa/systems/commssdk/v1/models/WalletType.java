package com.pahappa.systems.commssdk.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WalletType {
    LOCAL("Local"),
    INTERNATIONAL("International");

    private final String value;

    WalletType(String value) {
        this.value = value;
    }

    @Override @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static WalletType fromValue(String text) {
        for (WalletType type : WalletType.values()) {
            if (type.value.equals(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown wallet type value: " + text);
    }
}
