# CommsSDK for Java

A modern, easy-to-use Java SDK for sending SMS and querying balances via the Comms platform.

**Version:** 1.0.1
**Package:** `com.pahappa.systems.commssdk.v1`

---

## Installation

Add the SDK to your project as a dependency. If using Maven:

```xml
<dependency>
  <groupId>com.pahappa.systems</groupId>
  <artifactId>commssdk</artifactId>
  <version>1.0.1</version>
</dependency>
```

If using Gradle:

```groovy
implementation 'com.pahappa.systems:commssdk:1.0.1'
```

Or manually include the JAR in your classpath.

---

## Usage

### Basic Example

```java
import com.pahappa.systems.commssdk.v1.CommsSDK;
import com.pahappa.systems.commssdk.v1.models.MessagePriority;

public class Example {
    public static void main(String[] args) {
        // Authenticate
        CommsSDK sdk = CommsSDK.authenticate("your_username", "your_api_key");

        // Optional: Set sender ID
        sdk.withSenderId("MyBrand");

        // Send SMS
        boolean sent = sdk.sendSMS("+256700000001", "Hello from CommsSDK!");

        // Send with priority
        boolean sent2 = sdk.sendSMS(
            "+256700000002",
            "Urgent message",
            "MyBrand",
            MessagePriority.HIGHEST
        );

        // Query balance
        double balance = sdk.getBalance();

        System.out.println("Balance: " + balance);
    }
}
```

---

## Configuration

- **Environment:**
  By default, the SDK uses the live server. To use the sandbox (for testing):

  ```java
  CommsSDK.useSandBox();
  // ...authenticate and use as normal
  ```

  To switch back to live:
  ```java
  CommsSDK.useLiveServer();
  ```

- **Sender ID:**
  Set a custom sender ID (max 11 chars):
  ```java
  sdk.withSenderId("MyBrand");
  ```

---

## Error Handling

- Most methods print errors to `System.err` and return `false` or `null` on failure.
- For detailed error info, use the `querySendSMS` and `queryBalance` methods, which return an `ApiResponse` object with status and message fields.
- Common issues:
  - Not authenticated: Ensure you call `CommsSDK.authenticate(...)` before sending messages.
  - Invalid numbers or sender ID: Check your inputs and use the provided validators if needed.

---

## API Reference

### Static Methods

- `CommsSDK.authenticate(String userName, String apiKey): CommsSDK`
  Creates and authenticates a new SDK instance.

- `CommsSDK.useSandBox()`
  Switches to the sandbox environment (for testing).

- `CommsSDK.useLiveServer()`
  Switches to the live environment (for production).

### Instance Methods

- `CommsSDK withSenderId(String senderId)`
  Sets the sender ID for outgoing messages.

- `boolean sendSMS(String number, String message)`
- `boolean sendSMS(String number, String message, String senderId)`
- `boolean sendSMS(String number, String message, String senderId, MessagePriority priority)`
- `boolean sendSMS(List<String> numbers, String message)`
- `boolean sendSMS(List<String> numbers, String message, String senderId)`
- `boolean sendSMS(List<String> numbers, String message, MessagePriority priority)`
- `boolean sendSMS(List<String> numbers, String message, String senderId, MessagePriority priority)`
  Sends SMS to one or more numbers. Returns `true` if successful.

- `ApiResponse querySendSMS(List<String> numbers, String message, String senderId, MessagePriority priority)`
  Sends SMS and returns the full API response object.

- `double getBalance()`
  Returns your current SMS balance.

- `ApiResponse queryBalance()`
  Returns the full API response for balance queries.

### Properties

- `String getUserName()`
- `String getApiKey()`
- `String getSenderId()`
- `boolean isAuthenticated()`

---

## MessagePriority Enum

- `MessagePriority.HIGHEST`
- `MessagePriority.HIGH`
- `MessagePriority.MEDIUM`
- `MessagePriority.LOW`
- `MessagePriority.LOWEST`

---

## License

See [LICENSE](../LICENSE) for details.

---

## Support

For help or to request features, please contact [Pahappa Support](https://comms.egosms.co/contact) or open an issue in this repository.
