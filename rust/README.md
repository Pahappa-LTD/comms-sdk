# Comms Rust SDK
Rust implementation of the Comms SDK

## Usage

```rust
// Make object
let mut sdk = CommsSDK::new("YOUR_USERNAME", "YOUR_PASSWORD");
// or with a custom sender ID instead of the default Comms
let mut sdk = CommsSDK::new("YOUR_USERNAME", "YOUR_PASSWORD").with_sender_id("Custom ID");
// sdk.use_sandbox(); // Use this for testing at http://comms-test.pahappa.net/api/v1/json/
sdk.authenticate().unwrap(); //Returns a bool in case you need to use it
let numbers = vec!["256700000000"];
let message = "Test message from Rust";
let result = sdk.send_sms(numbers, message).unwrap();
```