# comms-sdk Python SDK

A Python implementation of the CommsSDK for sending SMS and managing communications, following the same patterns as the Ruby and Kotlin reference implementations.

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

Install the package from PyPI:

```bash
pip install comms-sdk
```

Or for development:

```bash
pip install .
```

---

## Usage

### Basic Authentication

```python
from comms_sdk.v1 import CommsSDK, MessagePriority

# Authenticate with username and API key
sdk = CommsSDK.authenticate("your_username", "your_api_key")
```

### Sending SMS

```python
# Send SMS to a single number
success = sdk.send_sms("0712345678", "Message to send")

# Send SMS to multiple numbers
success = sdk.send_sms(["0712345678", "0787654321"], "Message to many")

# Send SMS with custom sender ID and priority
success = sdk.send_sms(
    "0712345678",
    "Hello!",
    sender_id="MyApp",
    priority=MessagePriority.HIGH
)

# Get full API response
response = sdk.query_send_sms(
    ["0712345678"],
    "Hello!",
    "MyApp",
    MessagePriority.HIGHEST
)
```

### Checking Balance

```python
# Get balance as a float
balance = sdk.get_balance()
print(f"Balance: {balance}")

# Get full balance response
response = sdk.query_balance()
print(f"Status: {response.Status}")
print(f"Balance: {response.Balance}")
print(f"Currency: {response.Currency}")
```

### Configuration

```python
# Use sandbox environment
CommsSDK.use_sandbox()

# Use live server (default)
CommsSDK.use_live_server()

# Set custom sender ID
sdk.with_sender_id("MyCustomSender")
```

---

## API Reference

### CommsSDK

#### Static/Class Methods

- `CommsSDK.authenticate(user_name: str, api_key: str) -> CommsSDK`
  - Authenticate and return SDK instance.
- `CommsSDK.use_sandbox()`
  - Switch to sandbox environment.
- `CommsSDK.use_live_server()`
  - Switch to live environment.

#### Instance Methods

- `with_sender_id(sender_id: str) -> CommsSDK`
  - Set sender ID, returns self for chaining.
- `send_sms(numbers: str | List[str], message: str, sender_id: Optional[str] = None, priority: MessagePriority = MessagePriority.HIGHEST) -> bool`
  - Send SMS, returns boolean.
- `query_send_sms(numbers: List[str], message: str, sender_id: str, priority: MessagePriority) -> Optional[ApiResponse]`
  - Send SMS, returns full ApiResponse.
- `get_balance() -> Optional[float]`
  - Get account balance as float.
- `query_balance() -> Optional[ApiResponse]`
  - Get full balance response as ApiResponse.
- `set_authenticated()`
  - Mark SDK as authenticated (internal use).

#### Properties

- `api_key` - The API key used for authentication.
- `user_name` - The username used for authentication.
- `sender_id` - Current sender ID.
- `is_authenticated` - Authentication status.

### Models

#### MessagePriority

- `MessagePriority.HIGHEST` - Priority "0"
- `MessagePriority.HIGH` - Priority "1"
- `MessagePriority.MEDIUM` - Priority "2"
- `MessagePriority.LOW` - Priority "3"
- `MessagePriority.LOWEST` - Priority "4"

#### ApiResponse

- `Status` - Response status ("OK" or "Failed")
- `Message` - Response message
- `Cost` - Message cost
- `Currency` - Currency code
- `MsgFollowUpUniqueCode` - Unique tracking code
- `Balance` - Account balance

---

## Error Handling

The SDK raises appropriate Python exceptions:

```python
try:
    sdk = CommsSDK.authenticate("", "")  # Empty credentials
except ValueError as e:
    print(f"Authentication error: {e}")

try:
    sdk.send_sms([], "")  # Empty numbers and message
except ValueError as e:
    print(f"Validation error: {e}")
```

---

## Thread Safety

The SDK is thread-safe for read operations. For write operations or shared state modifications, use appropriate synchronization mechanisms.

---

## Contributing

Bug reports and pull requests are welcome on GitHub.

---

## License

The package is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).
