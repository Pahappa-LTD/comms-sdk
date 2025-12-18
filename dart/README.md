# CommsSDK Dart SDK

A Dart implementation of the CommsSDK for sending SMS and managing communications, following the same patterns as the Python, Ruby, and Kotlin reference implementations.

**Version:** 1.0.1

---

## Features

- Consistent API across all supported languages
- Authenticate with username and API key
- Send SMS to one or more recipients
- Optional sender ID and message priority
- Check account balance
- Comprehensive error handling

---

## Installation

Add the following to your `pubspec.yaml`:

```yaml
dependencies:
  comms_sdk: ^1.0.1
```

Then run:

```sh
dart pub get
```

---

## Usage

### Basic Authentication

```dart
import 'package:comms_sdk/comms_sdk.dart';

void main() async {
  // Authenticate with username and API key
  final sdk = await CommsSDK.authenticate('your_username', 'your_api_key');
}
```

### Sending SMS

```dart
// Send SMS to a single number
final success = await sdk.sendSMS(
  numbers: ['256712345678'],
  message: 'Hello from Dart!',
);

// Send SMS to multiple numbers with custom sender ID and priority
final multiSuccess = await sdk.sendSMS(
  numbers: ['256712345678', '256787654321'],
  message: 'Hello to all!',
  senderId: 'MyApp',
  priority: MessagePriority.HIGH,
);

// Get full API response
final response = await sdk.querySendSMS(
  numbers: ['256712345678'],
  message: 'Hello!',
  senderId: 'MyApp',
  priority: MessagePriority.HIGHEST,
);
```

### Checking Balance

```dart
// Get balance as a double
final balance = await sdk.getBalance();
print('Balance: $balance');

// Get full balance response
final balanceResponse = await sdk.queryBalance();
print('Status: ${balanceResponse?.status}');
print('Balance: ${balanceResponse?.balance}');
print('Currency: ${balanceResponse?.currency}');
```

### Configuration

```dart
// Use sandbox environment
CommsSDK.useSandBox();

// Use live server (default)
CommsSDK.useLiveServer();

// Set custom sender ID
sdk.withSenderId('MyCustomSender');
```

---

## API Reference

### CommsSDK

#### Static Methods

- `Future<CommsSDK> authenticate(String userName, String apiKey)`
  - Authenticate and return SDK instance.
- `void useSandBox()`
  - Switch to sandbox environment.
- `void useLiveServer()`
  - Switch to live environment.

#### Instance Methods

- `CommsSDK withSenderId(String senderId)`
  - Set sender ID, returns self for chaining.
- `Future<bool> sendSMS({required List<String> numbers, required String message, String? senderId, MessagePriority? priority})`
  - Send SMS, returns boolean.
- `Future<ApiResponse?> querySendSMS({required List<String> numbers, required String message, String? senderId, MessagePriority? priority})`
  - Send SMS, returns full ApiResponse.
- `Future<double?> getBalance()`
  - Get account balance as double.
- `Future<ApiResponse?> queryBalance()`
  - Get full balance response as ApiResponse.

#### Properties

- `String? userName` - The username used for authentication.
- `String? apiKey` - The API key used for authentication.
- `String senderId` - Current sender ID.
- `bool isAuthenticated` - Authentication status.

### Models

#### MessagePriority

- `MessagePriority.HIGHEST` - Priority "0"
- `MessagePriority.HIGH` - Priority "1"
- `MessagePriority.MEDIUM` - Priority "2"
- `MessagePriority.LOW` - Priority "3"
- `MessagePriority.LOWEST` - Priority "4"

#### ApiResponse

- `status` - Response status ("OK" or "Failed")
- `message` - Response message
- `cost` - Message cost
- `currency` - Currency code
- `msgFollowUpUniqueCode` - Unique tracking code
- `balance` - Account balance

---

## Error Handling

The SDK throws appropriate Dart exceptions:

```dart
try {
  final sdk = await CommsSDK.authenticate('', '');
} catch (e) {
  print('Authentication error: $e');
}

try {
  await sdk.sendSMS(numbers: [], message: '');
} catch (e) {
  print('Validation error: $e');
}
```

---

## Contributing

Bug reports and pull requests are welcome on GitHub.

---

## License

The package is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).
