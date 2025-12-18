# comms_sdk Ruby SDK

A Ruby implementation of the CommsSDK for sending SMS and managing communications, following the same patterns as the Python and Kotlin reference implementations.

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

Add this line to your application's Gemfile:

```ruby
gem 'comms_sdk', '~> 1.0.1'
```

And then execute:

```bash
bundle install
```

Or install it yourself as:

```bash
gem install comms_sdk
```

---

## Usage

### Basic Authentication

```ruby
require 'comms_sdk/v1'

# Authenticate with username and API key
sdk = CommsSdk::V1::CommsSDK.authenticate("your_username", "your_api_key")
```

### Sending SMS

```ruby
# Send SMS to a single number
success = sdk.send_sms("256712345678", "Hello from Ruby!")

# Send SMS to multiple numbers
success = sdk.send_sms(["256712345678", "256787654321"], "Hello to all!")

# Send SMS with custom sender ID and priority
success = sdk.send_sms(
  "256712345678",
  "Hello!",
  sender_id: "MyApp",
  priority: CommsSdk::V1::MessagePriority::HIGH
)

# Get full API response
response = sdk.query_send_sms(
  ["256712345678"],
  "Hello!",
  "MyApp",
  CommsSdk::V1::MessagePriority::HIGHEST
)
```

### Checking Balance

```ruby
# Get balance as a float
balance = sdk.get_balance
puts "Balance: #{balance}"

# Get full balance response
response = sdk.query_balance
puts "Status: #{response.status}"
puts "Balance: #{response.balance}"
puts "Currency: #{response.currency}"
```

### Configuration

```ruby
# Use sandbox environment
CommsSdk::V1::CommsSDK.use_sandbox

# Use live server (default)
CommsSdk::V1::CommsSDK.use_live_server

# Set custom sender ID
sdk.with_sender_id("MyCustomSender")
```

---

## API Reference

### CommsSdk::V1::CommsSDK

#### Class Methods

- `authenticate(user_name, api_key)`
  Authenticate and return SDK instance.
- `use_sandbox`
  Switch to sandbox environment.
- `use_live_server`
  Switch to live environment.

#### Instance Methods

- `with_sender_id(sender_id)`
  Set sender ID, returns self for chaining.
- `send_sms(numbers, message, sender_id: nil, priority: MessagePriority::HIGHEST)`
  Send SMS, returns boolean.
- `query_send_sms(numbers, message, sender_id, priority)`
  Send SMS, returns full ApiResponse.
- `get_balance`
  Get account balance as float.
- `query_balance`
  Get full balance response as ApiResponse.
- `set_authenticated`
  Mark SDK as authenticated (internal use).

#### Attributes

- `api_key`
  The API key used for authentication.
- `user_name`
  The username used for authentication.
- `sender_id`
  Current sender ID.
- `is_authenticated`
  Authentication status.

### Models

#### MessagePriority

- `MessagePriority::HIGHEST` - Priority "0"
- `MessagePriority::HIGH` - Priority "1"
- `MessagePriority::MEDIUM` - Priority "2"
- `MessagePriority::LOW` - Priority "3"
- `MessagePriority::LOWEST` - Priority "4"

#### ApiResponse

- `status` - Response status ("OK" or "Failed")
- `message` - Response message
- `cost` - Message cost
- `currency` - Currency code
- `msg_follow_up_unique_code` - Unique tracking code
- `balance` - Account balance

---

## Error Handling

The SDK raises appropriate Ruby exceptions:

```ruby
begin
  sdk = CommsSdk::V1::CommsSDK.authenticate("", "")  # Empty credentials
rescue ArgumentError => e
  puts "Authentication error: #{e.message}"
end

begin
  sdk.send_sms([], "")  # Empty numbers and message
rescue ArgumentError => e
  puts "Validation error: #{e.message}"
end
```

---

## Thread Safety

The SDK is thread-safe for read operations. For write operations or shared state modifications, use appropriate synchronization mechanisms.

---

## Contributing

Bug reports and pull requests are welcome on GitHub.

---

## License

The gem is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).
