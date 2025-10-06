# CommsSdk Ruby Implementation

A Ruby implementation of the Communications SDK that follows the same patterns as the Python implementation and matches the Kotlin reference implementation.

## Features

- **Consistent API**: Matches the standardized interface across all language implementations
- **Authentication**: Uses `userName` and `apiKey` authentication pattern
- **Dual Methods**: Provides both simple boolean return methods and full response query methods
- **Error Handling**: Comprehensive error handling with proper Ruby exceptions
- **Validation**: Phone number validation and credential validation
- **Logging**: Proper error logging to stderr

## Installation

Add this line to your application's Gemfile:

```ruby
gem 'comms_sdk'
```

And then execute:

    $ bundle install

Or install it yourself as:

    $ gem install comms_sdk

## Usage

### Basic Authentication

```ruby
require 'comms_sdk'

# Authenticate with username and API key
sdk = CommsSdk.authenticate("your_username", "your_api_key")

# Or use the V1 module directly
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

## API Reference

### CommsSdk::V1::CommsSDK

#### Class Methods

- `authenticate(user_name, api_key)` - Authenticate and return SDK instance
- `use_sandbox` - Switch to sandbox environment
- `use_live_server` - Switch to live environment

#### Instance Methods

- `send_sms(numbers, message, sender_id: nil, priority: MessagePriority::HIGHEST)` - Send SMS, returns boolean
- `query_send_sms(numbers, message, sender_id, priority)` - Send SMS, returns full ApiResponse
- `get_balance()` - Get account balance as float
- `query_balance()` - Get full balance response as ApiResponse
- `with_sender_id(sender_id)` - Set sender ID, returns self for chaining

#### Properties

- `api_key` - The API key used for authentication
- `user_name` - The username used for authentication  
- `sender_id` - Current sender ID
- `is_authenticated` - Authentication status

### Models

#### MessagePriority
- `HIGHEST` - Priority "0"
- `HIGH` - Priority "1" 
- `MEDIUM` - Priority "2"
- `LOW` - Priority "3"
- `LOWEST` - Priority "4"

#### ApiResponse
- `status` - Response status ("OK" or "Failed")
- `message` - Response message
- `cost` - Message cost
- `currency` - Currency code
- `msg_follow_up_unique_code` - Unique tracking code
- `balance` - Account balance

## Error Handling

The SDK raises appropriate Ruby exceptions:

```ruby
begin
  sdk = CommsSdk.authenticate("", "")  # Empty credentials
rescue ArgumentError => e
  puts "Authentication error: #{e.message}"
end

begin
  sdk.send_sms([], "")  # Empty numbers and message
rescue ArgumentError => e
  puts "Validation error: #{e.message}"
end
```

## Thread Safety

The SDK is thread-safe for read operations. For write operations or shared state modifications, use appropriate synchronization mechanisms.

## Contributing

Bug reports and pull requests are welcome on GitHub.

## License

The gem is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).