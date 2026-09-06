# CommsSDK

[![PyPI - Downloads](https://img.shields.io/pypi/dm/comms-sdk?label=PyPI%20downloads)](https://pypi.org/project/comms-sdk/)
[![RubyGems Downloads](https://img.shields.io/gem/dt/comms_sdk?label=RubyGems%20downloads)](https://rubygems.org/gems/comms_sdk)
[![Packagist Downloads](https://img.shields.io/packagist/dt/pahappa-limited/comms-sdk?label=Packagist%20downloads)](https://packagist.org/packages/pahappa-limited/comms-sdk)
[![Go Reference](https://pkg.go.dev/badge/github.com/Pahappa-LTD/comms-go-sdk.svg)](https://pkg.go.dev/github.com/Pahappa-LTD/comms-go-sdk)
[![NuGet](https://img.shields.io/nuget/dt/CommsSdk?label=NuGet%20downloads)](https://www.nuget.org/packages/CommsSdk/)
[![NPM Downloads](https://img.shields.io/npm/dm/@pahappalimited/comms-sdk?label=NPM%20downloads)](https://www.npmjs.com/package/@pahappalimited/comms-sdk)
[![Maven Central](https://img.shields.io/maven-central/v/com.pahappa.systems/comms-sdk?label=Maven%20Central)](https://central.sonatype.com/artifact/com.pahappa.systems/comms-sdk)
[![Pub.dev](https://img.shields.io/pub/v/comms_sdk?label=Pub.dev)](https://pub.dev/packages/comms_sdk)
[![Crates.io](https://img.shields.io/crates/v/comms_sdk?label=Crates.io)](https://crates.io/crates/comms_sdk)

A modern, multi-language SDK for sending SMS and querying balances via the EgoSMS Comms platform by Pahappa Limited.

**Version:** 1.1.0

---

## Features

- Unified API for SMS messaging and balance queries
- Official support for Java, Kotlin, JavaScript/TypeScript, Python, Ruby, PHP, Rust, Dart, C#, and Go
- Sandbox and live environments
- Type-safe models and error handling
- Actively maintained by [Pahappa Limited](https://pahappa.com)

---

## Quick Start

### 1. Clone the repository

```powershell
git clone https://github.com/pahappa-ltd/CommsSDK.git
cd CommsSDK
```

### 2. Initialize submodules

Some language SDKs are managed as submodules. Run:

```powershell
git submodule update --init --recursive
```

---

## Supported Languages

| Language      | SDK Path  | Docs/README                       |
| ------------- | --------- | --------------------------------- |
| Java          | `java/`   | [Java README](java/README.md)     |
| Kotlin        | `kotlin/` | [Kotlin README](kotlin/README.md) |
| JavaScript/TS | `js/`     | [JS/TS README](js/README.md)      |
| Python        | `python/` | [Python README](python/README.md) |
| Ruby          | `ruby/`   | [Ruby README](ruby/README.md)     |
| PHP           | `php/`    | [PHP README](php/README.md)       |
| Rust          | `rust/`   | [Rust README](rust/README.md)     |
| Dart          | `dart/`   | [Dart README](dart/README.md)     |
| C#            | `c#/`     | [C# README](c#/README.md)         |
| Go            | `go/`     | [Go README](go/README.md)         |

---

## Example Usage

Below are basic examples for several languages. See each language’s README for full details.

<details>
<summary>Java</summary>

```java
import com.pahappa.systems.commssdk.v1.CommsSDK;
CommsSDK sdk = CommsSDK.authenticate("your_username", "your_api_key");
sdk.sendSMS("+256700000001", "Hello from CommsSDK!");
```

</details>

<details>
<summary>Kotlin</summary>

```kotlin
val sdk = CommsSDK.authenticate("your_username", "your_api_key")
sdk.sendSMS("+256700000001", "Hello from CommsSDK!")
```

</details>

<details>
<summary>JavaScript/TypeScript</summary>

```typescript
import { v1 } from 'comms-sdk'
// or
import { CommsSDK } from 'comms-sdk/v1'
const sdk = CommsSDK.authenticate('your_username', 'your_api_key')
await sdk.sendSMS('+256700000001', 'Hello from CommsSDK!')
```

</details>

<details>
<summary>Python</summary>

```python
from comms_sdk import CommsSDK
sdk = CommsSDK.authenticate("your_username", "your_api_key")
sdk.send_sms("+256700000001", "Hello from CommsSDK!")
```

</details>

<details>
<summary>Ruby</summary>

```ruby
require 'comms_sdk'
sdk = CommsSdk::V1::CommsSDK.authenticate("your_username", "your_api_key")
sdk.send_sms("+256700000001", "Hello from CommsSDK!")
```

</details>

<details>
<summary>PHP</summary>

```php
use PahappaLimited\CommsSDK\v1\CommsSDK;
$sdk = CommsSDK::authenticate("your_username", "your_api_key");
$sdk->sendSMS("+256700000001", "Hello from CommsSDK!");
```

</details>

<details>
<summary>Rust</summary>

```rust
use comms_sdk::CommsSDK;
let mut sdk = CommsSDK::authenticate("your_username", "your_api_key");
sdk.send_sms("+256700000001", "Hello from CommsSDK!");
```

</details>

<details>
<summary>Dart</summary>

```dart
import 'package:comms_sdk/comms_sdk.dart';
final sdk = await CommsSDK.authenticate("your_username", "your_api_key");
await sdk.sendSMS(numbers: ["+256700000001"], message: "Hello from CommsSDK!");
```

</details>

<details>
<summary>C#</summary>

```csharp
using CommsSdk;
var sdk = await CommsSdk.Authenticate("your_username", "your_api_key");
await sdk.SendSms("+256700000001", "Hello from CommsSDK!");
```

</details>

<details>
<summary>Go</summary>

```go
import "github.com/Pahappa-LTD/comms-go-sdk/v1"
sdk := commssdk.Authenticate("your_username", "your_api_key")
sdk.SendSMS("+256700000001", "Hello from CommsSDK!")
```

</details>

---

## Roadmap

Planned API changes that will be rolled out to **every** language SDK. Each item below is
tracked per-language with its own checkbox; check one off only once that language's SDK has
actually implemented and tested the change.

> **Note:** [`TESTS.md`](TESTS.md) will be updated to add dedicated test IDs for each of the
> items below once the corresponding implementation work begins. Until then, `TESTS.md`
> describes only the current, shipped behaviour — it does not yet cover these planned changes.

### 1. Instance-based API URL (deprecate the global/static endpoint)

Today the active endpoint (live vs. sandbox) is process-global configuration shared by every
client instance (see `TESTS.md` §1, `ENV-*`). This will change so that each client instance
carries its own endpoint, fixed at creation time:

- Add `authenticate(username, apiKey)` — authenticates against the **live** endpoint
  (`https://comms.egosms.co/api/v1/json/`) and returns a client instance bound to it.
- Add `authenticateSandbox(username, apiKey)` — authenticates against the **sandbox** endpoint
  (`https://comms-test.pahappa.net/api/v1/json/`) and returns a client instance bound to it.
- Deprecate the static/global endpoint switches (e.g. `useSandBox()` / `useLiveServer()`, or each
  language's equivalent) in favor of the two constructors above. Existing global switches should
  keep working during a deprecation window, but new code should prefer the instance-based form.
- Two client instances created with different constructors (one live, one sandbox) must be able
  to coexist and operate independently in the same process, without one's endpoint choice
  affecting the other.

**Per-language status:**

- [ ] Java
- [ ] Kotlin
- [ ] JavaScript/TypeScript
- [ ] Python
- [ ] Ruby
- [ ] PHP
- [ ] Rust
- [ ] Dart
- [ ] C#
- [ ] Go

### 2. Custom messages per number

Today a single send call fans one message out to one or more numbers, always with the same text.
This adds support for sending **different** message text to different recipients in a single
call, via two new functions:

- A function that accepts a list of message entries (number + message text + sender ID +
  priority, i.e. the SDK's existing per-message model type) built directly by the caller. The
  caller is responsible for pre-cleaning each phone number into valid international format
  themselves (e.g. by running it through the SDK's existing number-validation helper) before
  constructing each entry — this function does **not** validate or normalize numbers on the
  caller's behalf, so an invalid number in the list will simply be rejected by the backend API
  rather than being caught client-side.
- A function that accepts a map/dictionary of phone number → message text. Here the SDK *does*
  validate and normalize each key using its existing number-validation logic before building the
  request. Any key that fails validation (doesn't match the accepted phone number shape) is
  **silently discarded, along with its associated message** — it is the caller's responsibility
  to supply numbers that will pass validation if they want them included. This mirrors the
  existing validation shape used elsewhere in the SDK: an optional leading `+`, followed by
  either a leading `0` or a 3-digit prefix, followed by exactly 9 more digits.

**Per-language status:**

- [ ] Java
- [ ] Kotlin
- [ ] JavaScript/TypeScript
- [ ] Python
- [ ] Ruby
- [ ] PHP
- [ ] Rust
- [ ] Dart
- [ ] C#
- [ ] Go

---

## Contributing

Pull requests are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) if available, or open an issue to discuss your idea.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## Support

For help or feature requests, contact [Pahappa Support](https://comms.egosms.co/contact) or open an issue on this repository.
