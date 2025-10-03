package com.pahappa.systems.commssdk.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import com.pahappa.systems.commssdk.v1.models.*;
import com.pahappa.systems.commssdk.v1.utils.NumberValidator;
import com.pahappa.systems.commssdk.v1.utils.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommsSDK {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static String API_URL = "http://176.58.101.43:8080/communications/api/v1/json/";

    @Getter
    private String userName;
    @Getter
    private String apiKey;

    @Getter @Setter
    private String senderId = "EgoSMS";
    @Setter
    private boolean isAuthenticated = false;
    private final RestTemplate client = new RestTemplate();

    private CommsSDK() {
    }

    public static CommsSDK authenticate(String userName, String apiKey) {
        CommsSDK sdk = new CommsSDK();
        sdk.userName = userName;
        sdk.apiKey = apiKey;
        Validator.validateCredentials(sdk);
        return sdk;
    }

    /**
     * Uses the sandbox url - useful for testing scenarios.
     * <br/>
     * Make an account at "<a href="http://sandbox.egosms.co">sandbox.egosms.co</a>" to use the sandbox.
     * Use {@link CommsSDK#useLiveServer()} for the live server.
     */
    public static void useSandBox() {
        API_URL = "http://176.58.101.43:8080/communications/api/v1/json";
    }

    /**
     * Uses the live url - useful for actual messaging scenarios.
     * <br/>
     * Make an account at "<a href="http://www.egosms.co">www.egosms.co</a>" to use the live server.
     * Use {@link CommsSDK#useSandBox()} for the sandbox server.
     */
    public static void useLiveServer() {
        API_URL = "http://176.58.101.43:8080/communications/api/v1/json";
    }

    public void setAuthenticated() {
        this.isAuthenticated = true;
    }

    public CommsSDK withSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    public boolean sendSMS(String number, String message) {
        return sendSMS(Collections.singletonList(number), message, senderId, MessagePriority.HIGHEST);
    }
    public boolean sendSMS(String number, String message, String senderId) {
        return sendSMS(Collections.singletonList(number), message, senderId, MessagePriority.HIGHEST);
    }
    public boolean sendSMS(String number, String message, String senderId, MessagePriority priority) {
        return sendSMS(Collections.singletonList(number), message, senderId, priority);
    }
    public boolean sendSMS(String number, String message, MessagePriority priority) {
        return sendSMS(Collections.singletonList(number), message, senderId, priority);
    }

    public boolean sendSMS(List<String> numbers, String message) {
        return sendSMS(numbers, message, senderId, MessagePriority.HIGHEST);
    }
    public boolean sendSMS(List<String> numbers, String message, String senderId) {
        return sendSMS(numbers, message, senderId, MessagePriority.HIGHEST);
    }
    public boolean sendSMS(List<String> numbers, String message, MessagePriority priority) {
        return sendSMS(numbers, message, senderId, priority);
    }
    public boolean sendSMS(List<String> numbers, String message, String senderId, MessagePriority priority) {
        ApiResponse apiResponse = querySendSMS(numbers, message, senderId, priority);
        if (apiResponse == null) {
            System.out.println("Failed to get a response from the server.");
            return false;
        }
        switch (apiResponse.getStatus()) {
            case OK:
                System.out.println("SMS sent successfully.");
                System.out.println("MessageFollowUpUniqueCode: " + apiResponse.getMessageFollowUpCode());
                return true;
            case Failed:
                System.out.println("Failed: " + apiResponse.getMessage());
                return false;
            default:
                throw new RuntimeException("Unexpected response status: " + apiResponse.getStatus());
        }
    }

    /** Same as {@link #sendSMS} but returns the full {@link ApiResponse} object. */
    public ApiResponse querySendSMS(List<String> numbers, String message, String senderId, MessagePriority priority) {
        if (sdkNotAuthenticated()) return null;
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("Numbers list cannot be empty");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (message.length() == 1) {
            throw new IllegalArgumentException("Message cannot be a single character");
        }
        if (senderId == null || senderId.trim().isEmpty()) {
            senderId = this.senderId;
        }
        if (senderId != null && senderId.length() > 11) {
            System.out.println("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.");
        }
        if (priority == null) {
            priority = MessagePriority.HIGHEST;
        }
        numbers = NumberValidator.validateNumbers(numbers);
        if (numbers.isEmpty()) {
            System.err.println("No valid phone numbers provided. Please check inputs.");
            return null;
        }
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("SendSms");
        List<MessageModel> messageModels = new ArrayList<>();
        for (String num : numbers) {
            MessageModel messageModel = new MessageModel();
            messageModel.setNumber(num);
            messageModel.setMessage(message);
            messageModel.setSenderId(senderId);
            messageModel.setPriority(priority);
            messageModels.add(messageModel);
        }
        apiRequest.setMessageData(messageModels);
        apiRequest.setUserdata(new UserData(userName, apiKey));
        ResponseEntity<String> res = client.postForEntity(API_URL, apiRequest, String.class);
        try {
            return OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            try {
                System.err.println("Request: " + OBJECT_MAPPER.writeValueAsString(apiRequest));
            } catch (Exception ignored) {}
            return null;
        }
    }

    private boolean sdkNotAuthenticated() {
        if (!isAuthenticated) {
            System.err.println("SDK is not authenticated. Please authenticate before performing actions.");
            System.err.println("Attempting to re-authenticate with provided credentials...");
            return !Validator.validateCredentials(this);
        }
        return false;
    }

    /** Same as {@link #getBalance} but returns the full {@link ApiResponse} object. */
    public ApiResponse queryBalance() {
        if (sdkNotAuthenticated()) {
            return null;
        }
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("Balance");
        apiRequest.setUserdata(new UserData(userName, apiKey));
        try {
            ResponseEntity<String> res = client.postForEntity(API_URL, apiRequest, String.class);
            return OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get balance: " + e.getMessage(), e);
        }
    }

    public Double getBalance() {
        ApiResponse response = queryBalance();
        return response != null ? response.getBalance() : null;
    }

    @Override
    public String toString() {
        return "SDK(" + userName + " => " + apiKey + ")";
    }
}
