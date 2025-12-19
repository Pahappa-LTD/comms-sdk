package com.pahappa.systems.commssdk.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pahappa.systems.commssdk.v1.models.*;
import com.pahappa.systems.commssdk.v1.utils.NumberValidator;
import com.pahappa.systems.commssdk.v1.utils.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Main entry point for interacting with the CommsSDK.
 * <p>
 * Provides methods for authentication, sending SMS, querying balances, and environment configuration.
 * </p>
 */
public class CommsSDK {

    /**
     * Shared Jackson object mapper for JSON serialization.
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * The API endpoint URL. Defaults to the live server.
     */
    public static String API_URL = "https://comms.egosms.co/api/v1/json/";

    @Getter
    private String userName;

    @Getter
    private String apiKey;

    @Getter
    @Setter
    private String senderId = "EgoSMS";

    @Setter
    private boolean isAuthenticated = false;

    private final RestTemplate client = new RestTemplate();

    /**
     * Private constructor. Use {@link #authenticate(String, String)} to create an instance.
     */
    private CommsSDK() {
    }

    /**
     * Authenticates and creates a new CommsSDK instance.
     *
     * @param userName Your account username.
     * @param apiKey   Your API key.
     * @return Authenticated CommsSDK instance.
     */
    public static CommsSDK authenticate(String userName, String apiKey) {
        CommsSDK sdk = new CommsSDK();
        sdk.userName = userName;
        sdk.apiKey = apiKey;
        Validator.validateCredentials(sdk);
        return sdk;
    }

    /**
     * Switches the SDK to use the sandbox environment (for testing).
     * <br>
     * Make an account at <a href="http://comms-test.pahappa.net">comms-test.pahappa.net</a> to use the sandbox.
     * Use {@link CommsSDK#useLiveServer()} for the live server.
     */
    public static void useSandBox() {
        API_URL = "https://comms-test.pahappa.net/api/v1/json";
    }

    /**
     * Switches the SDK to use the live environment (for production).
     * <br>
     * Make an account at <a href="http://comms.egosms.co">comms.egosms.co</a> to use the live server.
     * Use {@link CommsSDK#useSandBox()} for the sandbox server.
     */
    public static void useLiveServer() {
        API_URL = "https://comms.egosms.co/api/v1/json";
    }

    /**
     * Marks the SDK as authenticated.
     * <b>Not to be called manually!</b>
     */
    public void setAuthenticated() {
        this.isAuthenticated = true;
    }

    /**
     * Sets the sender ID for outgoing messages.
     *
     * @param senderId Sender ID (max 11 characters).
     * @return This CommsSDK instance (for chaining).
     */
    public CommsSDK withSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    /**
     * Sends an SMS to a single number with default sender ID and highest priority.
     *
     * @param number  Recipient phone number.
     * @param message Message text.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(String number, String message) {
        return sendSMS(
                Collections.singletonList(number),
                message,
                senderId,
                MessagePriority.HIGHEST
        );
    }

    /**
     * Sends an SMS to a single number with custom sender ID.
     *
     * @param number   Recipient phone number.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(String number, String message, String senderId) {
        return sendSMS(
                Collections.singletonList(number),
                message,
                senderId,
                MessagePriority.HIGHEST
        );
    }

    /**
     * Sends an SMS to a single number with custom sender ID and priority.
     *
     * @param number   Recipient phone number.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @param priority Message priority.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(
            String number,
            String message,
            String senderId,
            MessagePriority priority
    ) {
        return sendSMS(
                Collections.singletonList(number),
                message,
                senderId,
                priority
        );
    }

    /**
     * Sends an SMS to a single number with default sender ID and custom priority.
     *
     * @param number   Recipient phone number.
     * @param message  Message text.
     * @param priority Message priority.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(
            String number,
            String message,
            MessagePriority priority
    ) {
        return sendSMS(
                Collections.singletonList(number),
                message,
                senderId,
                priority
        );
    }

    /**
     * Sends an SMS to multiple numbers with default sender ID and highest priority.
     *
     * @param numbers List of recipient phone numbers.
     * @param message Message text.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(List<String> numbers, String message) {
        return sendSMS(numbers, message, senderId, MessagePriority.HIGHEST);
    }

    /**
     * Sends an SMS to multiple numbers with custom sender ID.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(
            List<String> numbers,
            String message,
            String senderId
    ) {
        return sendSMS(numbers, message, senderId, MessagePriority.HIGHEST);
    }

    /**
     * Sends an SMS to multiple numbers with default sender ID and custom priority.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param priority Message priority.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(
            List<String> numbers,
            String message,
            MessagePriority priority
    ) {
        return sendSMS(numbers, message, senderId, priority);
    }

    /**
     * Sends an SMS to multiple numbers with custom sender ID and priority.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @param priority Message priority.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(
            List<String> numbers,
            String message,
            String senderId,
            MessagePriority priority
    ) {
        ApiResponse apiResponse = querySendSMS(
                numbers,
                message,
                senderId,
                priority
        );
        if (apiResponse == null) {
            System.out.println("Failed to get a response from the server.");
            return false;
        }
        switch (apiResponse.getStatus()) {
            case OK:
                System.out.println("SMS sent successfully.");
                System.out.println(
                        "MessageFollowUpUniqueCode: " + apiResponse.getMessageFollowUpCode()
                );
                return true;
            case Failed:
                System.out.println("Failed: " + apiResponse.getMessage());
                return false;
            default:
                throw new RuntimeException(
                        "Unexpected response status: " + apiResponse.getStatus()
                );
        }
    }

    /**
     * Sends an SMS and returns the full API response object.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @param priority Message priority.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(
            List<String> numbers,
            String message,
            String senderId,
            MessagePriority priority
    ) {
        if (sdkNotAuthenticated()) return null;
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("Numbers list cannot be empty");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (message.length() == 1) {
            throw new IllegalArgumentException(
                    "Message cannot be a single character"
            );
        }
        if (senderId == null || senderId.trim().isEmpty()) {
            senderId = this.senderId;
        }
        if (senderId != null && senderId.length() > 11) {
            System.out.println(
                    "Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages."
            );
        }
        if (priority == null) {
            priority = MessagePriority.HIGHEST;
        }
        numbers = NumberValidator.validateNumbers(numbers);
        if (numbers.isEmpty()) {
            System.err.println(
                    "No valid phone numbers provided. Please check inputs."
            );
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
        ResponseEntity<String> res = client.postForEntity(
                API_URL,
                apiRequest,
                String.class
        );
        try {
            return OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            try {
                System.err.println(
                        "Request: " + OBJECT_MAPPER.writeValueAsString(apiRequest)
                );
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    /**
     * Checks if the SDK is authenticated. If not, attempts to re-authenticate.
     *
     * @return true if not authenticated, false if authenticated.
     */
    private boolean sdkNotAuthenticated() {
        if (!isAuthenticated) {
            System.err.println(
                    "SDK is not authenticated. Please authenticate before performing actions."
            );
            System.err.println(
                    "Attempting to re-authenticate with provided credentials..."
            );
            return !Validator.validateCredentials(this);
        }
        return false;
    }

    /**
     * Queries the balance and returns the full API response object.
     *
     * @return ApiResponse object with balance and details, or null on error.
     */
    public ApiResponse queryBalance() {
        if (sdkNotAuthenticated()) {
            return null;
        }
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("Balance");
        apiRequest.setUserdata(new UserData(userName, apiKey));
        try {
            ResponseEntity<String> res = client.postForEntity(
                    API_URL,
                    apiRequest,
                    String.class
            );
            return OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get balance: " + e.getMessage(), e);
        }
    }

    /**
     * Gets your current SMS account balance.
     *
     * @return Balance as a double.
     */
    public double getBalance() {
        return queryBalance().getBalance();
    }

    /**
     * Returns a string representation of the SDK instance.
     *
     * @return String representation.
     */
    @Override
    public String toString() {
        return "SDK(" + userName + " => " + apiKey + ")";
    }
}
