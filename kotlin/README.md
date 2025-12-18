# CommsSDK for Kotlin

A modern, type-safe SDK for sending SMS and querying balances via the EgoSMS Comms API, written in Kotlin.

**Version:** 1.0.1
**Package:** `comms-sdk`

---

## Installation

Add the SDK to your project using Gradle:

```kotlin
dependencies {
    implementation("com.pahappa.systems:comms-sdk:1.0.1")
}
```

Or with Maven:

```xml
<dependency>
    <groupId>com.pahappa.systems</groupId>
    <artifactId>comms-sdk</artifactId>
    <version>1.0.1</version>
</dependency>
```

---

## Usage

```kotlin
import v1.CommsSDK
import v1.models.MessagePriority

fun main() {
    // Authenticate
    val sdk = CommsSDK.authenticate("your_username", "your_api_key")

    // (Optional) Use sandbox environment for testing
    CommsSDK.useSandBox()
    // To switch back to live:
    // CommsSDK.useLiveServer()

    // (Optional) Set a custom sender ID
    sdk.withSenderId("MyBrand")

    // Send an SMS
    val success = sdk.sendSMS(
        numbers = listOf("+256700000001", "+256700000002"),
        message = "Hello from Kotlin SDK!",
        priority = MessagePriority.HIGHEST
    )
    println("SMS sent: $success")

    // Query balance
    val balance = sdk.getBalance()
    println("Balance: $balance")
}
```

---

## Configuration

- **Environment:**
  - `CommsSDK.useSandBox()` — Use the sandbox/test API (recommended for development).
  - `CommsSDK.useLiveServer()` — Use the live API (for production).

- **Sender ID:**
  - Set with `.withSenderId("YourSender")` on the SDK instance.
    Default is `"EgoSMS"`.

---

## Error Handling

- Most methods throw `IllegalArgumentException` for invalid input (e.g., empty numbers or message).
- Network/API errors are printed to stderr and may throw `RuntimeException`.
- Always check the return value of `sendSMS` (Boolean) and handle `null` from `querySendSMS` or `queryBalance`.

---

## API Reference

### Static Methods

- `CommsSDK.authenticate(userName: String, apiKey: String): CommsSDK`
  Create an authenticated SDK instance.

- `CommsSDK.useSandBox()`
  Switch to sandbox environment.

- `CommsSDK.useLiveServer()`
  Switch to live environment.

### Instance Methods

- `withSenderId(senderId: String): CommsSDK`
  Set the sender ID for outgoing messages.

- `sendSMS(number: String, message: String, senderId: String = this.senderId, priority: MessagePriority = MessagePriority.HIGHEST): Boolean`
  Send an SMS to a single number.

- `sendSMS(numbers: List<String>, message: String, senderId: String = this.senderId, priority: MessagePriority = MessagePriority.HIGHEST): Boolean`
  Send an SMS to multiple numbers.

- `querySendSMS(numbers: List<String>, message: String, senderId: String, priority: MessagePriority): ApiResponse?`
  Send SMS and get the full API response.

- `getBalance(): Double?`
  Get your SMS account balance.

- `queryBalance(): ApiResponse?`
  Get the full API response for balance.

### Properties

- `userName: String`
- `apiKey: String`
- `senderId: String`
- `isAuthenticated: Boolean`

---

## MessagePriority Enum

- `MessagePriority.HIGHEST`
- `MessagePriority.HIGH`
- `MessagePriority.MEDIUM`
- `MessagePriority.LOW`
- `MessagePriority.LOWEST`

---

## License

MIT or as specified in the repository.

---

## Support

For issues or feature requests, please open an issue on the [GitHub repository](https://github.com/pahappa/CommsSDK) or contact support at [EgoSMS](https://comms.egosms.co).
