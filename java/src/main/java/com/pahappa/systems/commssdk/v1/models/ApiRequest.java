package com.pahappa.systems.commssdk.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ApiRequest {
    @JsonProperty(required = true)
    private String method;
    @JsonProperty(required = true)
    private UserData userdata;
    @JsonProperty(value = "msgdata")
    private List<MessageModel> messageData;
}
