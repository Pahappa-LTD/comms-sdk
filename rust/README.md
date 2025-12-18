# CommsSDK Rust SDK

A Rust implementation of the CommsSDK for sending SMS and managing communications, following the same patterns as the Python and Ruby reference implementations.

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

Add this to your `Cargo.toml`:

```toml
[dependencies]
comms_sdk = "1.0.1"
```

Or, run:

```sh
cargo add comms_sdk
```

---

## Usage

### Basic Authentication

```rust
use comms_sdk::v1::{CommsSDK, MessagePriority};

fn main() {
    // Authenticate with username and API key
    let mut sdk = CommsSDK::authenticate("your_username", "your_api_key");

    // Send SMS to a single number
    let result = sdk.send_sms(vec!["256712345678"], "Hello from Rust!");
    match result {
        Ok(success) => println!("SMS sent: {}", success),
        Err(e) => eprintln!("Error sending SMS: {:?}", e),
    }

    // Send SMS to multiple numbers with custom sender ID and priority
    let mut sdk = sdk.with_sender_id("MyApp");
    let result = sdk.send_sms_full(
        vec!["256712345678", "256787654321"],
        "Hello to all!",
        "SenderID",
        MessagePriority::High,
    );
    match result {
        Ok(success) => println!("Bulk SMS sent: {}", success),
        Err(e) => eprintln!("Error sending bulk SMS: {:?}", e),
    }

    // Get account balance
    match sdk.get_balance() {
        Ok(balance) => println!("Balance: {}", balance),
        Err(e) => eprintln!("Error getting balance: {:?}", e),
    }
}
```

---

## Configuration

```rust
// Use sandbox environment for testing
CommsSDK::use_sandbox();

// Use live server (default)
CommsSDK::use_live_server();

// Set custom sender ID
let mut sdk = sdk.with_sender_id("MyCustomSender");
```

---

## API Reference

### CommsSDK

#### Associated Functions / Methods

- `CommsSDK::authenticate(user_name: &str, api_key: &str) -> CommsSDK`
  - Authenticate and return SDK instance.
- `CommsSDK::use_sandbox()`
  - Switch to sandbox environment.
- `CommsSDK::use_live_server()`
  - Switch to live environment.

#### Instance Methods

- `with_sender_id(&self, sender_id: &str) -> CommsSDK`
  - Set sender ID, returns a new SDK instance with the sender ID.
- `send_sms(&mut self, numbers: Vec<S>, message: T) -> Result<bool, Error>`
  - Send SMS to one or more numbers, returns boolean.
- `send_sms_full(&mut self, numbers: Vec<S>, message: T, sender_id: &str, priority: MessagePriority) -> Result<bool, Error>`
  - Send SMS with custom sender ID and priority, returns boolean.
- `query_send_sms(&mut self, numbers: Vec<S>, message: T) -> Result<ApiResponse, Error>`
  - Send SMS and get full API response.
- `query_send_sms_full(&mut self, numbers: Vec<S>, message: T, sender_id: &str, priority: MessagePriority) -> Result<ApiResponse, Error>`
  - Send SMS with custom sender ID and priority, get full API response.
- `get_balance(&mut self) -> Result<f64, Error>`
  - Get account balance as float.
- `query_balance(&mut self) -> Result<ApiResponse, Error>`
  - Get full balance response as ApiResponse.
- `is_authenticated(&self) -> bool`
  - Returns authentication status.

### Models

#### MessagePriority

- `MessagePriority::Highest` - Priority "0"
- `MessagePriority::High` - Priority "1"
- `MessagePriority::Medium` - Priority "2"
- `MessagePriority::Low` - Priority "3"
- `MessagePriority::Lowest` - Priority "4"

#### ApiResponse

- `status` - Response status (`ApiResponseCode::OK` or `ApiResponseCode::Failed`)
- `message` - Response message
- `cost` - Message cost
- `currency` - Currency code
- `message_follow_up_code` - Unique tracking code
- `balance` - Account balance

---

## Error Handling

All methods that perform network or validation operations return `Result<T, anyhow::Error>`. Handle errors using standard Rust error handling patterns:

```rust
match sdk.send_sms(vec!["256712345678"], "Hello!") {
    Ok(success) => println!("SMS sent: {}", success),
    Err(e) => eprintln!("Error: {}", e),
}
```

---

## Contributing

Bug reports and pull requests are welcome on GitHub.

---

## License

This crate is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).
