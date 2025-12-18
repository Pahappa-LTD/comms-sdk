# CommsSDK C# SDK

A C# implementation of the CommsSDK for sending SMS and managing communications, following the same patterns as the Python, Ruby, and Kotlin reference implementations.

**Version:** 1.0.1

---

## Features

- Consistent API across all supported languages
- Authenticate with username and API key
- Send SMS to one or more recipients (single or bulk)
- Optional sender ID and message priority
- Check account balance
- Comprehensive error handling

---

## Installation

Add the NuGet package (when available):

```
dotnet add package CommsSDK --version 1.0.1
```

Or reference the library project directly in your solution.

---

## Usage

### Basic Authentication

```csharp
using Comms;

var sdk = await CommsSdk.Authenticate("your_username", "your_api_key");
```

### Sending SMS

```csharp
// Send SMS to a single number
await sdk.SendSms("256712345678", "Hello from C#!");

// Send SMS to multiple numbers
var numbers = new List<string> { "256712345678", "256787654321" };
await sdk.SendSms(numbers, "Hello to all!");

// Send SMS with custom sender ID and priority
await sdk.SendSms(numbers, "Hello with custom sender!", "MyApp", MessagePriority.High);

// Get full API response
var response = await sdk.QuerySendSms(numbers, "Hello!", "MyApp", MessagePriority.Highest);
```

### Checking Balance

```csharp
// Get balance as a double
var balance = await sdk.GetBalance();
Console.WriteLine($"Balance: {balance}");

// Get full balance response
var balanceResponse = await sdk.QueryBalance();
Console.WriteLine($"Status: {balanceResponse?.Status}");
Console.WriteLine($"Balance: {balanceResponse?.Balance}");
Console.WriteLine($"Currency: {balanceResponse?.Currency}");
```

### Configuration

```csharp
// Use sandbox environment
CommsSdk.UseSandBox();

// Use live server (default)
CommsSdk.UseLiveServer();

// Set custom sender ID
sdk = sdk.WithSenderId("MyCustomSender");
```

---

## API Reference

### CommsSdk

#### Static Methods

- `Task<CommsSdk> Authenticate(string userName, string apiKey)`
  - Authenticate and return SDK instance (async).
- `void UseSandBox()`
  - Switch to sandbox environment.
- `void UseLiveServer()`
  - Switch to live environment.

#### Instance Methods

- `CommsSdk WithSenderId(string senderId)`
  - Set sender ID, returns new SDK instance with sender ID.
- `Task<bool> SendSms(string number, string message)`
  - Send SMS to a single number.
- `Task<bool> SendSms(List<string> numbers, string message)`
  - Send SMS to multiple numbers.
- `Task<bool> SendSms(string number, string message, string senderId)`
  - Send SMS to a single number with custom sender ID.
- `Task<bool> SendSms(List<string> numbers, string message, string senderId)`
  - Send SMS to multiple numbers with custom sender ID.
- `Task<bool> SendSms(string number, string message, string senderId, MessagePriority priority)`
  - Send SMS to a single number with custom sender ID and priority.
- `Task<bool> SendSms(List<string> numbers, string message, string senderId, MessagePriority priority)`
  - Send SMS to multiple numbers with custom sender ID and priority.
- `Task<ApiResponse?> QuerySendSms(List<string> numbers, string message, string senderId, MessagePriority priority)`
  - Send SMS and get full API response.
- `Task<double?> GetBalance()`
  - Get account balance as double.
- `Task<ApiResponse?> QueryBalance()`
  - Get full balance response as ApiResponse.
- `void SetAuthenticated()`
  - Mark SDK as authenticated (internal use).

#### Properties

- `string? UserName` - The username used for authentication.
- `string? ApiKey` - The API key used for authentication.
- `string SenderId` - Current sender ID.
- `bool IsAuthenticated` - Authentication status.

### Models

#### MessagePriority

- `MessagePriority.Highest` - Priority "0"
- `MessagePriority.High` - Priority "1"
- `MessagePriority.Medium` - Priority "2"
- `MessagePriority.Low` - Priority "3"
- `MessagePriority.Lowest` - Priority "4"

#### ApiResponse

- `Status` - Response status ("OK" or "Failed")
- `Message` - Response message
- `Cost` - Message cost
- `Currency` - Currency code
- `MsgFollowUpUniqueCode` - Unique tracking code
- `Balance` - Account balance

---

## Error Handling

All methods that perform network or validation operations return `Task<T>` and may throw exceptions. Use `try/catch` for error handling:

```csharp
try
{
    var sdk = await CommsSdk.Authenticate("user", "key");
    await sdk.SendSms("256712345678", "Test");
}
catch (Exception ex)
{
    Console.WriteLine($"Error: {ex.Message}");
}
```

---

## Contributing

Bug reports and pull requests are welcome on GitHub.

---

## License

This SDK is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).
