CommsSDK/js/README.md
# CommsSDK for JavaScript/TypeScript

**Version:** 1.0.1
**Package:** `comms-sdk` (TypeScript/JavaScript)

A modern, type-safe SDK for sending SMS and querying balances via the EgoSMS Comms API.
Supports both Node.js and browser environments (with appropriate polyfills for HTTP).

---

## Installation

```sh
npm install comms-sdk
# or
yarn add comms-sdk
# or
pnpm add comms-sdk
```

---

## Usage

### Basic Example

```typescript
import { CommsSDK, MessagePriority } from "comms-sdk";

// Authenticate
const sdk = CommsSDK.authenticate("your_username", "your_api_key");

// Optional: Use sandbox environment for testing
CommsSDK.useSandBox();

// Optional: Set a custom sender ID
sdk.withSenderId("MyBrand");

// Send an SMS
const success = await sdk.sendSMS(
  ["+256700000001", "+256700000002"],
  "Hello from CommsSDK JS!",
  "MyBrand", // optional, defaults to sdk.senderId
  MessagePriority.HIGHEST // optional, defaults to HIGHEST
);

console.log("SMS sent?", success);

// Query balance
const balance = await sdk.getBalance();
console.log("Your balance:", balance);
```

---

## Configuration

- **Environments:**
  - `CommsSDK.useSandBox()` — Use sandbox/test API (for development).
  - `CommsSDK.useLiveServer()` — Use live/production API (default).

- **Sender ID:**
  - Set globally per instance: `sdk.withSenderId("MyBrand")`
  - Or per message: pass as argument to `sendSMS`.

---

## Error Handling

- All async methods (`sendSMS`, `querySendSMS`, `getBalance`, `queryBalance`) throw on network or validation errors.
- If the API returns an error, methods return `false` or `null` and log details to the console.
- Always use `try/catch` or handle promise rejections.

```typescript
try {
  const ok = await sdk.sendSMS("+256700000001", "Test message");
  if (!ok) {
    console.error("Failed to send SMS (see logs for details)");
  }
} catch (err) {
  console.error("Unexpected error:", err);
}
```

---

## API Reference

### Static Methods

| Method | Description |
|--------|-------------|
| `CommsSDK.authenticate(userName: string, apiKey: string): CommsSDK` | Create an authenticated SDK instance. |
| `CommsSDK.useSandBox(): void` | Switch to sandbox API endpoint. |
| `CommsSDK.useLiveServer(): void` | Switch to live API endpoint. |

---

### Instance Methods

| Method | Description |
|--------|-------------|
| `withSenderId(senderId: string): CommsSDK` | Set sender ID for this instance. |
| `sendSMS(numbers: string \| string[], message: string, senderId?: string, priority?: MessagePriority): Promise<boolean>` | Send SMS. Returns `true` if successful. |
| `querySendSMS(numbers: string[], message: string, senderId: string, priority: MessagePriority): Promise<ApiResponse \| null>` | Send SMS and get full API response. |
| `getBalance(): Promise<number \| null>` | Get account balance (returns `null` on error). |
| `queryBalance(): Promise<ApiResponse \| null>` | Get full balance API response. |

---

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `userName` | `string` | Authenticated username. |
| `apiKey` | `string` | Authenticated API key. |
| `senderId` | `string` | Default sender ID. |
| `isAuthenticated` | `boolean` | Whether the SDK is authenticated. |

---

### Types

#### `MessagePriority`
- `MessagePriority.HIGHEST`
- `MessagePriority.HIGH`
- `MessagePriority.MEDIUM`
- `MessagePriority.LOW`
- `MessagePriority.LOWEST`

#### `ApiResponse`
- `Status`: `"OK"` or `"Failed"`
- `Message`: string
- `MsgFollowUpUniqueCode`: string (for sent messages)
- `Balance`: number (for balance queries)
- ...other fields as per API

---

## Advanced

- **Custom HTTP:** Uses `axios` for HTTP requests. You can polyfill or swap if needed.
- **Validation:** Phone numbers and credentials are validated before sending.
- **TypeScript:** Fully typed for safety and autocompletion.

---

## License

MIT

---

## Support

For issues or feature requests, open an issue on the [GitHub repository](https://github.com/your-org/CommsSDK).

---
