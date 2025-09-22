package com.pahappa.systems.egosmssdk.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiResponse {
    @JsonProperty(required = true, value = "Status")
    private ApiResponseCode status;
    @JsonProperty(value = "Message")
    private String message;
    @JsonProperty(value = "Cost")
    private String cost;
    @JsonProperty(value = "MsgFollowUpUniqueCode")
    private String messageFollowUpCode;
    @JsonProperty(value = "Balance")
    private String balance;
}
