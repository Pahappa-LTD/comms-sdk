package com.pahappa.systems.commssdk.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserData {
    private final String username;
    @JsonProperty("password")
    private final String apikey;

    public UserData(String userName, String apiKey) {
        this.username = userName;
        this.apikey = apiKey;
    }
}
