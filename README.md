# CommsSDK

[![PyPI - Downloads](https://img.shields.io/pypi/dm/comms-sdk?label=PyPI%20downloads)](https://pypi.org/project/comms-sdk/)
[![RubyGems Downloads](https://img.shields.io/gem/dt/comms_sdk?label=RubyGems%20downloads)](https://rubygems.org/gems/comms_sdk)
[![Packagist Downloads](https://img.shields.io/packagist/dt/pahappa-limited/comms-sdk?label=Packagist%20downloads)](https://packagist.org/packages/pahappa-limited/comms-sdk)
[![Go Reference](https://pkg.go.dev/badge/github.com/Pahappa-LTD/comms-go-sdk.svg)](https://pkg.go.dev/github.com/Pahappa-LTD/comms-go-sdk)
[![NuGet](https://img.shields.io/nuget/dt/CommsSdk?label=NuGet%20downloads)](https://www.nuget.org/packages/CommsSdk/)
[![NPM Downloads](https://img.shields.io/npm/dm/comms-sdk?label=NPM%20downloads)](https://www.npmjs.com/package/comms-sdk)
[![Maven Central](https://img.shields.io/maven-central/v/com.pahappa.systems/comms-sdk?label=Maven%20Central)](https://central.sonatype.com/artifact/com.pahappa.systems/comms-sdk)
[![Pub.dev](https://img.shields.io/pub/v/comms_sdk?label=Pub.dev)](https://pub.dev/packages/comms_sdk)
[![Crates.io](https://img.shields.io/crates/v/comms_sdk?label=Crates.io)](https://crates.io/crates/comms_sdk)

A modern, multi-language SDK for sending SMS and querying balances via the EgoSMS Comms platform by Pahappa Limited.

**Version:** 1.0.1

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

```sh
git clone https://github.com/pahappa-ltd/CommsSDK.git
cd CommsSDK
```

### 2. Initialize submodules

Some language SDKs are managed as submodules. Run:

```sh
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

## Contributing

Pull requests are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) if available, or open an issue to discuss your idea.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## Support

For help or feature requests, contact [Pahappa Support](https://comms.egosms.co/contact) or open an issue on this repository.
