package com.pahappa.systems.egosmssdk.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import com.pahappa.systems.egosmssdk.v1.models.*;
import com.pahappa.systems.egosmssdk.v1.utils.NumberValidator;
import com.pahappa.systems.egosmssdk.v1.utils.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EgoSmsSDK {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static String API_URL = "https://www.egosms.co/api/v1/json/";

    @Getter
    private String apiKey;
    @Getter
    private String username;
    @Getter
    private String password;

    @Getter @Setter
    private String senderId = "EgoSms";
    @Setter
    private boolean isAuthenticated = false;
    private final RestTemplate client = new RestTemplate();

    private EgoSmsSDK() {
    }

    public static EgoSmsSDK authenticate(String apiKey) {
//        EgoSmsSDK sdk = new EgoSmsSDK();
//        sdk.apiKey = apiKey;
//        Validator.validateCredentials(sdk);
//        return sdk;
        throw new UnsupportedOperationException("API Key authentication is not supported in this version. Please use username and password authentication.");
    }

    public static EgoSmsSDK authenticate(String username, String password) {
        EgoSmsSDK sdk = new EgoSmsSDK();
        sdk.username = username;
        sdk.password = password;
        Validator.validateCredentials(sdk);
        return sdk;
    }

    /**
     * Uses the sandbox url - useful for testing scenarios.
     * <br/>
     * Make an account at "<a href="http://sandbox.egosms.co">sandbox.egosms.co</a>" to use the sandbox.
     * Use {@link EgoSmsSDK#useLiveServer()} for the live server.
     */
    public static void useSandBox() {
        API_URL = "http://sandbox.egosms.co/api/v1/json/";
    }

    /**
     * Uses the live url - useful for actual messaging scenarios.
     * <br/>
     * Make an account at "<a href="http://www.egosms.co">www.egosms.co</a>" to use the live server.
     * Use {@link EgoSmsSDK#useSandBox()} for the sandbox server.
     */
    public static void useLiveServer() {
        API_URL = "https://www.egosms.co/api/v1/json/";
    }

    public EgoSmsSDK withSenderId(String senderId) {
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
        if (sdkNotAuthenticated()) return false;
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("Numbers list cannot be null or empty");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
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
            return false;
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
        apiRequest.setUserdata(new UserData(username, password));
        ResponseEntity<String> res = client.postForEntity(API_URL, apiRequest, String.class);
        try {
            ApiResponse apiResponse = OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
            switch (apiResponse.getStatus()) {
                case OK:
                    System.out.println("SMS sent successfully.");
                    System.out.println("MessageFollowUpUniqueCode: " + apiResponse.getMessageFollowUpCode());
                    return true;
                case Failed:
                    throw new Exception(apiResponse.getMessage());
                default:
                    throw new RuntimeException("Unexpected response status: " + apiResponse.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            try {
                System.err.println("Request: " + OBJECT_MAPPER.writeValueAsString(apiRequest));
            } catch (Exception ignored) {}
            return false;
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

    public String getBalance() {
        if (sdkNotAuthenticated()) {
            return null;
        }
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("Balance");
        apiRequest.setUserdata(new UserData(username, password));
        try {
            ResponseEntity<String> res = client.postForEntity(API_URL, apiRequest, String.class);
            ApiResponse response = OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
            System.out.println("MessageFollowUpUniqueCode: " + response.getMessageFollowUpCode());
            return response.getBalance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get balance: " + e.getMessage(), e);
        }
    }
}
