package com.pahappa.systems.commssdk.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pahappa.systems.commssdk.v1.models.*;
import com.pahappa.systems.commssdk.v1.utils.NumberValidator;
import com.pahappa.systems.commssdk.v1.utils.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.pahappa.systems.commssdk.v1.utils.Log.println;

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
    private static final String LIVE_API_URL = "https://comms.egosms.co/api/v1/json";
    private static final String SANDBOX_API_URL = "https://comms-test.pahappa.net/api/v1/json";
    /**
     * Temporary since we are shifting the SDK's API URL to be object-based and not a static field.
     */
    @Deprecated
    private static CommsSDK instance = new CommsSDK();
    /**
     * The API endpoint URL. Defaults to the live server.
     */
    @Getter
    private String apiUrl = LIVE_API_URL;

    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private String apiKey;

    @Getter
    @Setter
    private String senderId = "EgoSMS";

    @Getter
    private boolean isAuthenticated = false;

    private final RestTemplate client = new RestTemplate();

    /**
     * Private constructor. Use {@link #authenticate(String, String)} to create an instance.
     */
    private CommsSDK() {}

    public CommsSDK(String userName, String apiKey) {
        this.userName = userName;
        this.apiKey = apiKey;
    }

    public CommsSDK authenticate() {
        apiUrl = LIVE_API_URL;
        isAuthenticated = Validator.validateCredentials(this);
        return this;
    }

    public CommsSDK authenticateSandbox() {
        apiUrl = SANDBOX_API_URL;
        isAuthenticated = Validator.validateCredentials(this);
        return this;
    }

    /**
     * Authenticates and creates a new CommsSDK instance.
     * <br/>
     * Deprecated, use this instead:
     * <pre>
     *     {@code
     *         CommsSDK sdk = new CommsSDK(user, pass);
     *         sdk.authenticate();
     *         // or if using the sandbox:
     *         sdk.authenticateSandbox();
     *      }
     * </pre>
     *
     * @param userName Your account username.
     * @param apiKey   Your API key.
     * @return Authenticated CommsSDK instance.
     */
    @Deprecated
    public static CommsSDK authenticate(String userName, String apiKey) {
        instance.userName = userName;
        instance.apiKey = apiKey;
        instance.isAuthenticated = Validator.validateCredentials(instance);
        return instance;
    }

    /**
     * Switches the SDK to use the sandbox environment (for testing).
     * <br/>
     * Deprecated, use this instead:
     * <pre>
     *     {@code
     *         CommsSDK sdk = new CommsSDK(user, pass);
     *         sdk.authenticate();
     *         // or if using the sandbox:
     *         sdk.authenticateSandbox();
     *      }
     * </pre>
     * <br>
     * Make an account at <a href="http://comms-test.pahappa.net">comms-test.pahappa.net</a> to use the sandbox.
     * Use {@link CommsSDK#useLiveServer()} for the live server.
     */
    @Deprecated
    public static void useSandBox() {
        instance.apiUrl = SANDBOX_API_URL;
    }

    /**
     * Switches the SDK to use the live environment (for production).
     * <br>
     * Deprecated, use this instead:
     * <pre>
     *     {@code
     *         CommsSDK sdk = new CommsSDK(user, pass);
     *         sdk.authenticate();
     *         // or if using the sandbox:
     *         sdk.authenticateSandbox();
     *      }
     * </pre>
     * Make an account at <a href="http://comms.egosms.co">comms.egosms.co</a> to use the live server.
     * Use {@link CommsSDK#useSandBox()} for the sandbox server.
     */
    @Deprecated
    public static void useLiveServer() {
        instance.apiUrl = LIVE_API_URL;
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
     * Sends an SMS to a single number with default sender ID and priority.
     *
     * @param number  Recipient phone number.
     * @param message Message text.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(String number, String message) {
        return sendSMS(Collections.singletonList(number), message, senderId, MessagePriority.HIGH);
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
        return sendSMS(Collections.singletonList(number), message, senderId, MessagePriority.HIGH);
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
    public boolean sendSMS(String number, String message, String senderId, MessagePriority priority) {
        return sendSMS(Collections.singletonList(number), message, senderId, priority);
    }

    /**
     * Sends an SMS to a single number with default sender ID and custom priority.
     *
     * @param number   Recipient phone number.
     * @param message  Message text.
     * @param priority Message priority.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(String number, String message, MessagePriority priority) {
        return sendSMS(Collections.singletonList(number), message, senderId, priority);
    }

    /**
     * Sends an SMS to multiple numbers with default sender ID and priority.
     *
     * @param numbers List of recipient phone numbers.
     * @param message Message text.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(List<String> numbers, String message) {
        return sendSMS(numbers, message, senderId, MessagePriority.HIGH);
    }

    /**
     * Sends an SMS to multiple numbers with custom sender ID.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(List<String> numbers, String message, String senderId) {
        return sendSMS(numbers, message, senderId, MessagePriority.HIGH);
    }

    /**
     * Sends an SMS to multiple numbers with default sender ID and custom priority.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param priority Message priority.
     * @return true if sent successfully, false otherwise.
     */
    public boolean sendSMS(List<String> numbers, String message, MessagePriority priority) {
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
    public boolean sendSMS(List<String> numbers, String message, String senderId, MessagePriority priority) {
        ApiResponse apiResponse = querySendSMS(numbers, message, senderId, priority);
        if (apiResponse == null) {
            println("Failed to get a response from the server.");
            return false;
        }
        switch (apiResponse.getStatus()) {
            case OK:
                println("SMS sent successfully.");
                println("MessageFollowUpUniqueCode: " + apiResponse.getMessageFollowUpCode());
                return true;
            case Failed:
                println("Failed: " + apiResponse.getMessage());
                return false;
            default:
                throw new RuntimeException("Unexpected response status: " + apiResponse.getStatus());
        }
    }

    /**
     * Sends an SMS to a single number with default sender ID and priority, returning the full API response.
     *
     * @param number  Recipient phone number.
     * @param message Message text.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(String number, String message) {
        return querySendSMS(Collections.singletonList(number), message, senderId, MessagePriority.HIGH);
    }

    /**
     * Sends an SMS to a single number with custom sender ID, returning the full API response.
     *
     * @param number   Recipient phone number.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(String number, String message, String senderId) {
        return querySendSMS(Collections.singletonList(number), message, senderId, MessagePriority.HIGH);
    }

    /**
     * Sends an SMS to a single number with custom sender ID and priority, returning the full API response.
     *
     * @param number   Recipient phone number.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @param priority Message priority.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(String number, String message, String senderId, MessagePriority priority) {
        return querySendSMS(Collections.singletonList(number), message, senderId, priority);
    }

    /**
     * Sends an SMS to a single number with default sender ID and custom priority, returning the full API response.
     *
     * @param number   Recipient phone number.
     * @param message  Message text.
     * @param priority Message priority.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(String number, String message, MessagePriority priority) {
        return querySendSMS(Collections.singletonList(number), message, senderId, priority);
    }

    /**
     * Sends an SMS to multiple numbers with default sender ID and priority, returning the full API response.
     *
     * @param numbers List of recipient phone numbers.
     * @param message Message text.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(List<String> numbers, String message) {
        return querySendSMS(numbers, message, senderId, MessagePriority.HIGH);
    }

    /**
     * Sends an SMS to multiple numbers with custom sender ID, returning the full API response.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param senderId Sender ID.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(List<String> numbers, String message, String senderId) {
        return querySendSMS(numbers, message, senderId, MessagePriority.HIGH);
    }

    /**
     * Sends an SMS to multiple numbers with default sender ID and custom priority, returning the full API response.
     *
     * @param numbers  List of recipient phone numbers.
     * @param message  Message text.
     * @param priority Message priority.
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse querySendSMS(List<String> numbers, String message, MessagePriority priority) {
        return querySendSMS(numbers, message, senderId, priority);
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
            println("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.");
        }
        if (priority == null) {
            priority = MessagePriority.HIGH;
        }
        numbers = NumberValidator.validateNumbers(numbers);
        if (numbers.isEmpty()) {
            println("No valid phone numbers provided. Please check inputs.");
            return null;
        }
        List<MessageModel> messageModels = new ArrayList<>();
        for (String num : numbers) {
            MessageModel messageModel = new MessageModel();
            messageModel.setNumber(num);
            messageModel.setMessage(message);
            messageModel.setSenderId(senderId);
            messageModel.setPriority(priority);
            messageModels.add(messageModel);
        }
        return sendCustomSMS(messageModels);
    }

    /**
     * This method accepts a custom-built list of {@link MessageModel} objects, for maximum flexibility.
     *
     * @param messages Custom-built list of message objects to be sent to the API
     * @return ApiResponse object with status and details, or null on error.
     */
    public ApiResponse sendCustomSMS(List<MessageModel> messages) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("SendSms");
        apiRequest.setMessageData(messages);
        apiRequest.setUserdata(new UserData(userName, apiKey));
        apiRequest.setWalletType(WalletType.LOCAL);
        ResponseEntity<String> res = sendAsContentTypeJson(apiRequest);
        try {
            return OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
        } catch (Exception e) {
            println("Failed to send SMS: " + e.getMessage());
            try {
                println("Request: " + OBJECT_MAPPER.writeValueAsString(apiRequest));
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private @NonNull ResponseEntity<String> sendAsContentTypeJson(ApiRequest apiRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ApiRequest> entity = new HttpEntity<>(apiRequest, headers);
        return client.postForEntity(apiUrl, entity, String.class);
    }

    /**
     * Checks if the SDK is authenticated. If not, attempts to re-authenticate.
     *
     * @return true if not authenticated, false if authenticated.
     */
    private boolean sdkNotAuthenticated() {
        if (!isAuthenticated) {
            println("SDK is not authenticated. Please authenticate before performing actions.");
            println("Attempting to re-authenticate with provided credentials...");
            isAuthenticated = Validator.validateCredentials(this);
            return !isAuthenticated;
        }
        return false;
    }

    /**
     * Queries the local wallet balance and returns the full API response object.
     *
     * @return ApiResponse object with balance and details, or null on error.
     */
    public ApiResponse queryBalance() {
        return queryBalance(WalletType.LOCAL);
    }

    /**
     * Queries the balance for the given wallet and returns the full API response object.
     *
     * @param walletType Which wallet to query. Defaults to {@link WalletType#LOCAL} if null.
     * @return ApiResponse object with balance and details, or null on error.
     */
    public ApiResponse queryBalance(WalletType walletType) {
        if (sdkNotAuthenticated()) {
            return null;
        }
        if (walletType == null) {
            walletType = WalletType.LOCAL;
        }
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("Balance");
        apiRequest.setUserdata(new UserData(userName, apiKey));
        apiRequest.setWalletType(walletType);
        try {
            ResponseEntity<String> res = sendAsContentTypeJson(apiRequest);
            return OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get balance: " + e.getMessage(), e);
        }
    }

    /**
     * Gets your current local wallet SMS account balance.
     *
     * @return Balance as a double.
     */
    public double getBalance() {
        return getBalance(WalletType.LOCAL);
    }

    /**
     * Gets the SMS account balance for the given wallet.
     *
     * @param walletType Which wallet to query. Defaults to {@link WalletType#LOCAL} if null.
     * @return Balance as a double.
     */
    public double getBalance(WalletType walletType) {
        return queryBalance(walletType).getBalance();
    }

    /**
     * For tests
     */
    void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    static CommsSDK getInstance() {
        return instance;
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
