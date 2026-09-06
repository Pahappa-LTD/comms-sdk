# CommsSDK — Cross-Language Test Specification

This is the **canonical, self-contained test specification for every CommsSDK language port**
(Java, Kotlin, JS/TS, Python, Ruby, PHP, Rust, Dart, C#, Go).

Every SDK talks to the same HTTP API and must put **byte-identical JSON on the wire** for the
same inputs. This document defines both that JSON contract and the test suite that proves each
port honours it. You should be able to write a compliant test suite for a new language port from
this file alone, without reading any SDK's source code.

**How to use this document:**

- Section 0 is the API contract — the ground truth every test asserts against.
- Sections 1–13 are the test suites. Every test has a stable ID (`AUTH-1`, `WIRE-3`, …).
- Every ID must have a corresponding test in **every** SDK. Test names may be idiomatic per
  language (`test_auth_1_...`, `Auth1_...`, `it('AUTH-1 ...')`), but the ID must appear in the
  test name or an adjacent comment so coverage can be diffed mechanically across ports.
- If a port genuinely cannot implement an ID (no method overloading, no checked exceptions, …),
  it must still test the equivalent behaviour and record the deviation in section 16.

---

## 0. The API contract (ground truth)

### 0.1 Endpoints

| Environment | URL |
| --- | --- |
| **Live / production** | `https://comms.egosms.co/api/v1/json/` |
| **Sandbox / testing** | `https://comms-test.pahappa.net/api/v1/json/` |

Both accept `POST` with `Content-Type: application/json`. The live endpoint is the SDK default;
the sandbox is opt-in. Sign up for sandbox access at `comms-test.pahappa.net` and for live
access at `comms.egosms.co`.

### 0.2 Request envelope

Every request — send or balance — is a single JSON object with this shape:

```jsonc
{
  "method":   "SendSms" | "Balance",   // REQUIRED
  "userdata": {                        // REQUIRED
    "username": "YOUR_USERNAME",       // REQUIRED — the API username
    "password": "YOUR_API_KEY"         // REQUIRED — the API key, sent under the name "password"
  },
  "msgdata": [                         // REQUIRED for SendSms; null or absent for Balance
    {
      "number":   "256700111222",      // REQUIRED — international format, no "+", no spaces
      "message":  "Hello World!",      // REQUIRED — the SMS text
      "senderid": "EgoSMS",            // OPTIONAL to the API; can be null
      "priority": "1"                  // OPTIONAL to the API; can be null. If provided, the API
                                       // accepts EITHER an integer 0–4 OR that digit as a quoted
                                       // string. 0 = highest, 1 = default. The SDK always sends
                                       // the quoted-string form — see WIRE-8.
    }
  ],
  "walletType": "Local" | "International"  // OPTIONAL to the API, but NEVER null if present.
                                            // The SDK always sends it, defaulting to "Local".
}
```

Field-by-field rules, as enforced by the real API:

| Field | Required | Notes |
| --- | --- | --- |
| `method` | always | Exactly `"SendSms"` or `"Balance"`. Case-sensitive. |
| `userdata` | always | Object. Present on both methods. |
| `userdata.username` | always | String. The API username. |
| `userdata.password` | always | String. **The API key goes here** — the wire field is named `password`, not `apikey`. |
| `msgdata` | on `SendSms` | Array of message objects, one per recipient. For `Balance` it must be `null` or absent. |
| `msgdata[].number` | always | String in international format, digits only (e.g. `256700111222`). No leading `+`, no spaces, no hyphens. |
| `msgdata[].message` | always | String. The SMS body. |
| `msgdata[].senderid` | optional to API | String, and **may be null**. If omitted or null, the API uses the account's configured default sender ID. **The SDK always sends a non-null value** (default `"EgoSMS"`) so behaviour doesn't depend on per-account configuration. |
| `msgdata[].priority` | optional to API | A value in the range `0`–`4`, where `0` is the highest priority and `4` the lowest; the API's own default is `1`. May be null or omitted. **The API accepts both forms** — the bare integer `1` and the quoted string `"1"` are equivalent to it. Because this spec requires identical wire output from all ten ports, one form is pinned as canonical: **the SDK always sends the quoted string** (default `"1"`). See `WIRE-8`. |
| `walletType` | optional to API | `"Local"` or `"International"`. Ignored on `SendSms`; honoured on `Balance`. **Must never be an explicit `null`** — an absent field and an explicit null are not equivalent to this API. **The SDK always sends `"Local"`** unless the caller chooses otherwise, on *both* methods. |

### 0.3 Canonical request examples

Minimal `SendSms` accepted by the API (all optional fields omitted):

```bash
curl -X POST https://comms.egosms.co/api/v1/json/ \
  -H "Content-Type: application/json" \
  -d '{
    "method": "SendSms",
    "userdata": {
      "username": "YOUR_USERNAME",
      "password": "YOUR_API_KEY"
    },
    "msgdata": [
      {
        "number": "256700111222",
        "message": "Hello World!"
      }
    ]
  }'
```

Minimal `Balance` accepted by the API:

```bash
curl -X POST https://comms.egosms.co/api/v1/json/ \
  -H "Content-Type: application/json" \
  -d '{
    "method": "Balance",
    "userdata": {
      "username": "YOUR_USERNAME",
      "password": "YOUR_API_KEY"
    }
  }'
```

**What the SDK actually sends** is the fuller form — it always populates `senderid`, `priority`
and `walletType` rather than relying on API-side defaults, so behaviour is identical across all
ten ports regardless of per-account configuration. See `WIRE-1` and `WIRE-13` for the exact
expected bodies.

### 0.4 Response envelope

```jsonc
{
  "Status":  "OK" | "Failed",   // always present; note the capitalised key
  "Message": "Success",          // human-readable detail; on failure, the reason
  "Cost": 0.5,                   // SendSms only, optional
  "MsgFollowUpUniqueCode": "ABC123",  // SendSms only, optional — the tracking code
  "Balance": 100.0               // Balance only, optional
}
```

Response keys are **capitalised** (`Status`, not `status`). The `Status` *value* is parsed
case-insensitively by the SDK (`"OK"`, `"ok"`, `"Failed"`, `"failed"` all parse), but the SDK
serializes it back as exactly `OK` / `Failed`.

Successful send:

```json
{"Status":"OK","Message":"Success","MsgFollowUpUniqueCode":"ABC123","Cost":0.5}
```

Failed send:

```json
{"Status":"Failed","Message":"Insufficient balance"}
```

Successful balance:

```json
{"Status":"OK","Message":"Success","Balance":100.0}
```

Rejected credentials:

```json
{"Status":"Failed","Message":"Invalid credentials"}
```

### 0.5 SDK-level defaults (identical in every port)

| Thing | Default |
| --- | --- |
| Endpoint | live — `https://comms.egosms.co/api/v1/json/` |
| Sender ID | `"EgoSMS"` |
| Priority | `HIGH`, which serializes as the quoted string `"1"` |
| Wallet type | `LOCAL`, which serializes as `"Local"` |
| Log prefix | `[CommsSDK]: ` |

---

## 1. Test architecture (required in every SDK)

Each SDK's suite has three layers:

| Layer | Network | Runs in CI | Purpose |
| --- | --- | --- | --- |
| **Unit** | none | always | Pure helpers: number validation, enum (de)serialization, model equality, logging. |
| **Mocked integration** | in-process loopback HTTP server | always | Full request/response cycle against a fake Comms API; asserts the exact bytes on the wire. |
| **Live sandbox smoke** | real `https://comms-test.pahappa.net/api/v1/json/` | only when `COMMS_SANDBOX_USERNAME` and `COMMS_SANDBOX_API_KEY` are set | Proves a real customer can authenticate and send. |

### 1.1 Mock server requirements (`MOCK-*`)

Every port needs a small test double standing in for the real Comms API.

- `MOCK-1` It must be a **real HTTP server bound to `127.0.0.1` on an ephemeral port** — not a
  mock/stub of the SDK's internal HTTP client. Stubbing the client hides exactly the bugs this
  suite exists to catch: a field that stops being serialized, wrong key casing, `null` emitted
  where the key should be absent, or a `Content-Type` that silently becomes `application/xml`.
  Those only appear in the actual bytes sent over a socket.
- `MOCK-2` The SDK's API URL is repointed at `http://127.0.0.1:<port>/` for the duration of each
  test and restored to its original value in teardown, regardless of test outcome.
- `MOCK-3` It captures every raw request body it receives and exposes: all captured bodies in
  order (`capturedBodies()`), and the most recent one (`lastCapturedBody()`).
- `MOCK-4` It picks its canned response by reading the JSON `method` field of the incoming
  request:
  - `"Balance"` → a balance response,
  - `"SendSms"` → a send response,
  - anything else → `{"Status":"Failed","Message":"Unknown method"}`.
- `MOCK-5` It exposes mutable knobs a test can set before making a call:

  | Knob | Default | Effect |
  | --- | --- | --- |
  | `balanceStatus` | `"OK"` | `"OK"` → `{"Status":"OK","Message":"Success","Balance":<balanceValue>}`; anything else → `{"Status":"Failed","Message":"Invalid credentials"}` |
  | `balanceValue` | `100.0` | The `Balance` number in a successful balance response |
  | `sendSmsStatus` | `"OK"` | `"OK"` → `{"Status":"OK","Message":"<sendSmsMessage>","MsgFollowUpUniqueCode":"ABC123","Cost":0.5}`; anything else → `{"Status":"Failed","Message":"<sendSmsMessage>"}` |
  | `sendSmsMessage` | `"Success"` | The `Message` string in the send response |

- `MOCK-6` It responds `HTTP 200` with `Content-Type: application/json` for these canned
  responses. Error-status scenarios are driven separately in `RES-*`.
- `MOCK-7` It is shut down in teardown and the SDK's API URL is restored, so no test leaks state
  into another. **This matters because the API URL is process-global configuration, not
  per-instance** — see `ENV-5`.

### 1.2 Live sandbox gating (`GATE-*`)

- `GATE-1` Credential-dependent live tests read exactly these two environment variables:

  ```
  COMMS_SANDBOX_USERNAME
  COMMS_SANDBOX_API_KEY
  ```

  They are never hardcoded in source. When either is missing, empty, or whitespace-only, the test
  **skips — it does not fail**. Use the language's skip primitive (`Assume.assumeTrue`,
  `pytest.skip`, `t.Skip`, `Skip.If`, `pending`, …) rather than an assertion, so the suite stays
  green for contributors without sandbox access, CI included.
- `GATE-2` Every live test explicitly switches to the sandbox itself, rather than relying on test
  execution order or on a previous test having switched. It must **never** touch
  `https://comms.egosms.co/api/v1/json/`.
- `GATE-3` The wrong-credentials test (`LIVE-1`) needs no real credentials and costs nothing, so
  it always runs, unskipped, even without sandbox access.

The repo-root test runner loads these variables from a `.env` file at the repository root if one
is present; see `.env.example`.

---

## 2. Environment & configuration (`ENV-*`)

The SDK targets one of two endpoints, selected by explicit calls, and the selection persists
until changed.

| ID | Case | Expected |
| --- | --- | --- |
| `ENV-1` | Fresh state, before any environment switch | the API URL is `https://comms.egosms.co/api/v1/json/` (live is the default) |
| `ENV-2` | Switch to sandbox (`useSandBox()` or the port's equivalent) | the API URL becomes `https://comms-test.pahappa.net/api/v1/json/`, and every subsequent request goes there |
| `ENV-3` | Switch to live (`useLiveServer()` or the port's equivalent) | the API URL becomes `https://comms.egosms.co/api/v1/json/` |
| `ENV-4` | Switch to sandbox, then back to live, repeatedly | the **last** switch wins; switching is idempotent and safe to repeat |
| `ENV-5` | The API URL is process-global, not per-instance | switching it affects **all** client instances, including ones constructed *before* the switch. Construct a client, switch the endpoint, then make a call and assert the call went to the new endpoint. This is surprising behaviour, so it is tested explicitly rather than left implicit. |
| `ENV-6` | The two URL strings are exact | assert the literal strings in `ENV-1`/`ENV-2`/`ENV-3` character for character, including the trailing slash. All ten ports must agree on these two strings; a port that normalizes or trims them is a cross-port defect. |

---

## 3. Authentication (`AUTH-*`)

`authenticate(username, apiKey)` is a factory: it constructs a client instance **and immediately
performs a real `Balance` round-trip** to confirm the credentials work before marking the client
authenticated. There is no separate login endpoint — a successful `Balance` call *is* the
credential check.

| ID | Case | Expected |
| --- | --- | --- |
| `AUTH-1` | Valid credentials; mock returns `{"Status":"OK","Message":"Success","Balance":100.0}` | returns a client whose authenticated flag is `true` |
| `AUTH-2` | Mock returns `{"Status":"Failed","Message":"Invalid credentials"}` | a client instance is **still returned** — never null, never a thrown exception — but its authenticated flag is `false` |
| `AUTH-3` | The credential-check request body | `"method"` is `"Balance"`, not `"SendSms"` |
| `AUTH-4` | The credential-check request's `walletType` | the key is **present**, **not null**, and equals `"Local"`. Assert all three separately: `body.has("walletType")`, `!body.get("walletType").isNull()`, `body.get("walletType") == "Local"`. The API treats an explicit `"walletType": null` differently from an absent key and rejects the former, so a port that starts emitting null here breaks authentication silently. |
| `AUTH-5` | The credential-check request's `userdata` | `userdata.username` is the username and `userdata.password` is the **API key**. Assert the key is literally spelled `password` — not `apikey`, `api_key`, or `apiKey`. |
| `AUTH-6` | `username` is null | raises a clear argument error before any network call; assert the mock captured **zero** requests |
| `AUTH-7` | `apiKey` is null | same as `AUTH-6` |
| `AUTH-8` | Network error during the credential check (point the SDK at a closed port) | the error is caught internally; a client is returned with authenticated `false`; **no exception escapes** `authenticate()` |
| `AUTH-9` | Mock returns a non-JSON body (e.g. `<html>502 Bad Gateway</html>`) | same handling as `AUTH-8` |
| `AUTH-10` | An unauthenticated client is used to send an SMS or query a balance | the SDK retries the credential check **once** before giving up. With the mock still failing, the operation returns "no result" (null/None/nil/`Option::None`) rather than throwing. Assert the retry happened by checking a second `Balance` request was captured. |
| `AUTH-11` | Same as `AUTH-10`, but flip `balanceStatus` to `"OK"` before the call | the retry succeeds, the authenticated flag flips to `true`, and the original operation proceeds and returns a real result |
| `AUTH-12` | Debug/string representation of a client | a stable documented format containing the username — assert the exact format your port produces and keep it consistent across ports. Because this can expose the API key, treat it as a deliberate debug surface distinct from logging (see `LOG-4`). |

---

## 4. Sender ID (`SENDER-*`)

The sender ID is the alphanumeric label recipients see. It has an instance default, overridable
per call, and always ends up on the wire as `msgdata[].senderid`.

| ID | Case | Expected |
| --- | --- | --- |
| `SENDER-1` | Freshly authenticated client, nothing configured | `msgdata[].senderid` is `"EgoSMS"` |
| `SENDER-2` | Fluent setter (`withSenderId("MyBrand")` or equivalent) | returns the **same instance** so calls chain, and later sends carry `"senderid":"MyBrand"` |
| `SENDER-3` | Plain setter (`setSenderId("MyBrand")`) | later sends carry `"senderid":"MyBrand"` |
| `SENDER-4` | A send call given an explicit sender ID argument | that value appears on the wire **for that call only**; a subsequent call with no explicit sender ID falls back to the instance default. Assert both captured bodies. |
| `SENDER-5` | A send call given `null`, `""`, or `"   "` as the sender ID | falls back to the instance default; the wire value is never `""` and never `null` |
| `SENDER-6` | Sender ID longer than 11 characters (e.g. `"TwelveCharss"`) | a warning is logged, but the message **is still sent** — this is not a validation error, and the long value goes out on the wire as given |
| `SENDER-7` | Sender ID of exactly 11 characters (e.g. `"ElevenChar"` padded to 11) | no warning is logged |

---

## 5. Phone number validation (`NUM-*`)

Before any number reaches a request, the SDK normalizes and validates it. The pipeline is:

1. Trim surrounding whitespace.
2. Strip **all** hyphens (`-`) and internal whitespace.
3. Match against the regex `^\+?(0|\d{3})\d{9}$` — an optional leading `+`, then either a
   leading `0` **or** a 3-digit prefix, then exactly 9 more digits.
4. If it starts with `0`, replace that `0` with `256` (the Uganda country code).
   Otherwise, if it starts with `+`, strip the `+`.
5. Add to a set, which de-duplicates.

Anything failing step 3 is logged and dropped from the batch. The batch is never aborted because
of one bad entry.

| ID | Input | Expected output |
| --- | --- | --- |
| `NUM-1` | `"256712345678"` | `"256712345678"` — already international, unchanged |
| `NUM-2` | `"+256712345678"` | `"256712345678"` — leading `+` stripped |
| `NUM-3` | `"0712345678"` | `"256712345678"` — leading `0` replaced with `256` |
| `NUM-4` | `"235-787-900-123"` | `"235787900123"` — hyphens stripped; valid because **any** 3-digit prefix passes, not just `256` |
| `NUM-5` | `"+257 700 567 234"` | `"257700567234"` — spaces stripped, `+` stripped |
| `NUM-6` | `"0745"` | rejected — too short; logged and excluded |
| `NUM-7` | `null`, `""`, or `"   "` as an element of the list | that element rejected and logged; the remaining elements are still processed |
| `NUM-8` | `null` or an empty list passed to the validator | returns an **empty list** and logs a message — does not throw |
| `NUM-9` | `["0712345678", "256712345678", "+256712345678"]` | exactly **one** entry, `"256712345678"` — all three normalize to the same number and de-duplicate |
| `NUM-10` | `["256712345678", "+256712345678", "0712345678", "235-787-900-123", "+257 700 567 234", "0745"]` | exactly **3** entries: `"256712345678"` (from the first three, de-duplicated), `"235787900123"`, and `"257700567234"`. `"0745"` is dropped. |
| `NUM-11` | Ordering of the result | de-duplication uses an unordered set, so **order is not guaranteed**. Tests must assert on membership and count — never on index position. |
| `NUM-12` | `"+0712345678"` | `"0712345678"` — the `0`-prefix branch is checked **before** the `+`-strip branch, so a value with both is only `+`-stripped and is *not* expanded to `256…`. Assert this exact behaviour; it's an easy spot for ports to silently diverge. |
| `NUM-13` | `"2567123456789"` (13 digits) or `"abcdefghijk"` | rejected and logged, like `NUM-6` |
| `NUM-14` | A send call where **every** number is invalid, e.g. `["0745", "abc"]` | **no HTTP request is made** (assert zero captured bodies); the call returns "no result" and logs `No valid phone numbers provided. Please check inputs.` |
| `NUM-15` | A send call with `["256712345678", "0745", "0712345678"]` | the request goes out with exactly **2** entries in `msgdata` — the two valid, normalized, de-duplicated numbers. The invalid one is absent from the wire entirely. |

---

## 6. Send-SMS argument validation (`ARG-*`)

These checks run **before** any network call. In every case below, assert the mock captured
**zero** request bodies for the send attempt.

| ID | Case | Expected |
| --- | --- | --- |
| `ARG-1` | The recipient list is `null` | raises an argument error, message `Numbers list cannot be empty` |
| `ARG-2` | The recipient list is empty (`[]`) | same error as `ARG-1` |
| `ARG-3` | The message is `null` | raises an argument error, message `Message cannot be empty` |
| `ARG-4` | The message is `""` | same error as `ARG-3` |
| `ARG-5` | The message is exactly one character (`"x"`) | raises an argument error, message `Message cannot be a single character` |
| `ARG-6` | The message is exactly two characters (`"hi"`) | **accepted** and sent — the boundary immediately above `ARG-5` |
| `ARG-7` | Priority omitted or `null` | defaults to `HIGH`, serialized as `"priority":"1"` |
| `ARG-8` | The client is not authenticated | argument validation is never reached; the call instead follows the re-authentication path in `AUTH-10` and returns "no result" if that fails |

The validation order matters and should be asserted: the authentication check (`ARG-8`) comes
first, then numbers (`ARG-1`/`ARG-2`), then message (`ARG-3`–`ARG-5`), then sender ID and
priority defaulting, and only then number normalization (`NUM-14`).

---

## 7. Wire format (`WIRE-*`)

This is the highest-value section: it is what keeps ten independently-written SDKs
byte-compatible with one backend. Every test here parses the raw body captured by the mock
server and asserts on the resulting JSON.

### 7.1 Send-SMS request

Given a client authenticated as `user` / `key` with default settings, calling
`sendSMS("256700000000", "Hello world")` must put **exactly** this on the wire:

```json
{
  "method": "SendSms",
  "userdata": {
    "username": "user",
    "password": "key"
  },
  "msgdata": [
    {
      "number": "256700000000",
      "message": "Hello world",
      "senderid": "EgoSMS",
      "priority": "1"
    }
  ],
  "walletType": "Local"
}
```

| ID | Assertion |
| --- | --- |
| `WIRE-1` | The complete body matches the JSON above (key-for-key, value-for-value; key order is irrelevant, presence and values are not) |
| `WIRE-2` | `method` is exactly the string `"SendSms"` — capital `S`, lowercase `ms` |
| `WIRE-3` | `walletType` is **present**, **not null**, and `"Local"`. Assert presence and non-nullness separately from the value, because `{"walletType": null}` and an absent key are distinct to the API. |
| `WIRE-4` | `userdata.username` == `"user"` and `userdata.password` == `"key"`. The API key is under `password`; assert there is no `apikey`/`api_key`/`apiKey` key anywhere in the body. |
| `WIRE-5` | `msgdata` is a JSON **array**, with exactly one element per validated, de-duplicated number |
| `WIRE-6` | Each `msgdata` element has exactly these four keys and no others: `number`, `message`, `senderid`, `priority` |
| `WIRE-7` | The sender-ID key is spelled `senderid` — **all lowercase**, not `senderId` or `sender_id` |
| `WIRE-8` | `priority` is the **quoted string digit** `"1"` — a JSON string, not a number, and never the enum's symbolic name (`"HIGH"` / `"High"` are always wrong). The API itself accepts both `"1"` and the bare integer `1`, so this is a *consistency* requirement rather than a compatibility one: pinning one form is what keeps all ten ports byte-identical. Assert the parsed node's JSON **type** is a string, not merely that its textual value is `1` — in most JSON libraries a naive text comparison passes for both forms and would let a port silently drift to integers. Full mapping in `MODEL-1`. |
| `WIRE-9` | For a 3-number send, `msgdata` has 3 elements, each with the **same** `message`, `senderid`, and `priority`, differing only in `number` |
| `WIRE-10` | The request carries the header `Content-Type: application/json`. Assert this from the mock server's received headers — some HTTP clients default to form or XML content types, and the SDK must force JSON. |
| `WIRE-11` | A message containing Unicode and emoji (e.g. `"Habari 👋 Zanzíbar"`) is UTF-8 encoded and round-trips byte-identical through the captured body |
| `WIRE-12` | A message containing `"` , `\`, and embedded newlines (e.g. `"He said \"hi\"\\nbye"`) is correctly JSON-escaped and decodes back to the exact original string |

### 7.2 Balance request

Calling `queryBalance()` on the same client must put **exactly** this on the wire:

```json
{
  "method": "Balance",
  "userdata": {
    "username": "user",
    "password": "key"
  },
  "walletType": "Local"
}
```

| ID | Assertion |
| --- | --- |
| `WIRE-13` | The complete body matches the JSON above |
| `WIRE-14` | `method` is exactly `"Balance"` |
| `WIRE-15` | `msgdata` is **absent, or explicitly `null`** — a balance query never carries message data. Both forms are accepted by the API; pick one per port, assert it, and note in section 16 which form your port emits so the difference is visible across ports. |
| `WIRE-16` | `walletType` is present, not null, and `"Local"` by default — same three-part assertion as `WIRE-3` |
| `WIRE-17` | Same `Content-Type: application/json` requirement as `WIRE-10` |

### 7.3 Response parsing

| ID | Assertion |
| --- | --- |
| `WIRE-18` | `{"Status":"OK",...}` and `{"Status":"ok",...}` both parse to the success outcome; `{"Status":"Failed",...}` and `{"Status":"failed",...}` both parse to the failure outcome — the status value is matched **case-insensitively** |
| `WIRE-19` | `{"Status":"Weird"}` raises a clear error naming the unknown value (e.g. `Unknown value: Weird`) rather than silently defaulting to success or failure |
| `WIRE-20` | From `{"Status":"OK","Message":"Success","MsgFollowUpUniqueCode":"ABC123","Cost":0.5}`: the message, the tracking code, and the cost are all readable from the parsed result. The wire key `MsgFollowUpUniqueCode` is exposed under a friendlier name (e.g. `messageFollowUpCode`); assert the mapping. |
| `WIRE-21` | From `{"Status":"OK","Message":"Success","Balance":100.0}`: the balance is readable as a number |
| `WIRE-22` | From `{"Status":"OK"}` alone: `Cost`, `Balance`, and `MsgFollowUpUniqueCode` are null/absent on the result — **never silently coerced to `0`**, and never a parse error |
| `WIRE-23` | From `{"Status":"OK","Message":"Success","SomeNewField":123,"Nested":{"a":1}}`: unknown extra fields do **not** break deserialization |

---

## 8. Send-SMS behaviour (`SEND-*`)

### 8.1 Boolean convenience API (`sendSMS` — "did it send?")

| ID | Case | Expected |
| --- | --- | --- |
| `SEND-1` | `sendSmsStatus = "OK"` | returns `true`; the `MsgFollowUpUniqueCode` (`"ABC123"`) is logged |
| `SEND-2` | `sendSmsStatus = "Failed"`, `sendSmsMessage = "Insufficient balance"` | returns `false` **without throwing**; `"Insufficient balance"` is logged |
| `SEND-3` | The underlying full-response call yields "no result" | returns `false` and logs `Failed to get a response from the server.` |
| `SEND-4` | Every recipient number is invalid | returns `false` via the `SEND-3` path; no request is sent (see `NUM-14`) |

### 8.2 Full-response API (`querySendSMS` — returns the parsed result)

| ID | Case | Expected |
| --- | --- | --- |
| `SEND-5` | `sendSmsStatus = "OK"` | returns a result with success status, tracking code `"ABC123"`, and cost `0.5` |
| `SEND-6` | `sendSmsStatus = "Failed"` | returns a result with failure status and the server's `Message` — **does not throw**. The caller inspects the status; failure is a value, not an exception. |
| `SEND-7` | The mock returns a malformed/non-JSON body | returns "no result"; the parse failure is logged along with the outgoing request; **no exception escapes** |
| `SEND-8` | The client is unauthenticated and re-authentication fails | returns "no result" (see `AUTH-10`) |

### 8.3 Overload / arity matrix

Every port must expose the full convenience matrix below, in whatever form is idiomatic
(overloads, optional/default/named parameters, a builder, or an options object). **Both** the
boolean-style and full-response-style calls must support each shape:

| ID | Signature shape |
| --- | --- |
| `SEND-9` | single number, message |
| `SEND-10` | single number, message, sender ID |
| `SEND-11` | single number, message, priority |
| `SEND-12` | single number, message, sender ID, priority |
| `SEND-13` | list of numbers, message |
| `SEND-14` | list of numbers, message, sender ID |
| `SEND-15` | list of numbers, message, priority |
| `SEND-16` | list of numbers, message, sender ID, priority |

`SEND-17`: a call given the single number `"256700000000"` must produce a **byte-identical wire
payload** to the same call given the one-element list `["256700000000"]`. Assert this by
capturing both bodies and comparing the parsed JSON directly.

---

## 9. Balance (`BAL-*`)

Two wallets exist: `LOCAL` (wire value `"Local"`) and `INTERNATIONAL` (wire value
`"International"`).

| ID | Case | Expected |
| --- | --- | --- |
| `BAL-1` | `queryBalance()` with no argument, `balanceValue = 42.0` | sends `"walletType":"Local"`; the parsed result has status OK and balance `42.0` |
| `BAL-2` | `queryBalance(LOCAL)` | wire value is exactly `"Local"` |
| `BAL-3` | `queryBalance(INTERNATIONAL)` | wire value is exactly `"International"` |
| `BAL-4` | `queryBalance(null)` | falls back to `"Local"` — a null wallet is **never** serialized onto the wire (see `WIRE-16`) |
| `BAL-5` | `getBalance()` with `balanceValue = 17.75` | returns `17.75`, compared with a small floating-point tolerance (e.g. `0.0001`) |
| `BAL-6` | `getBalance(INTERNATIONAL)` | sends `"walletType":"International"` and returns that wallet's amount |
| `BAL-7` | `balanceValue = 0.0`, and a fractional value like `0.25` | both parse exactly; a zero balance is **never** treated as missing, null, or an error |
| `BAL-8` | Unauthenticated client, re-authentication fails | `queryBalance()` returns "no result" |
| `BAL-9` | Network error or unparseable response during `queryBalance()` | **raises an error** to the caller, with a message like `Failed to get balance: <cause>`. Note the deliberate asymmetry: `querySendSMS` returns "no result" for the same class of failure (`SEND-7`), while `queryBalance` throws. Preserve this in every port rather than silently unifying the two styles — callers written against one port must behave the same on another. |
| `BAL-10` | `getBalance()` invoked when the underlying `queryBalance()` would yield "no result" (e.g. `BAL-8`) | each port must pick **one** well-defined behaviour — propagate a clear error, or return a defined sentinel — and **all ten ports must do the same thing**. Assert whichever was chosen; record it in section 16 if your port differs. |

---

## 10. Enums & models (`MODEL-*`)

| ID | Case | Expected |
| --- | --- | --- |
| `MODEL-1` | Message priority — serializing | five levels mapping to the digits `0`–`4`: `HIGHEST` ↔ `0`, `HIGH` ↔ `1`, `MEDIUM` ↔ `2`, `LOW` ↔ `3`, `LOWEST` ↔ `4`. `0` is the most urgent and `4` the least. `HIGH` is the SDK default when no priority is given. Serializing must produce the **quoted string** form (`"1"`), per `WIRE-8`. |
| `MODEL-2` | Message priority — parsing | parsing must accept **both** wire forms, since the API documents both as valid: the quoted `"1"` and the bare integer `1` both parse to `HIGH`. A port that only handles its own output form would break on a response or stored payload written by another port. |
| `MODEL-3` | Parsing priority `9`, `-1`, or a non-numeric value | raises a clear error naming the bad input, e.g. `Unknown priority value: 9` — the valid range is exactly `0`–`4` |
| `MODEL-4` | Wallet type | `LOCAL` ↔ `"Local"` and `INTERNATIONAL` ↔ `"International"`, both directions, **exact casing** |
| `MODEL-5` | Parsing wallet type `"local"` (lowercase) | **raises** — this mapping is case-*sensitive*, deliberately unlike the response-status mapping in `WIRE-18`, which is case-*insensitive*. The two are different kinds of field: one is a value the SDK sends, the other a value the server sends. |
| `MODEL-6` | Response status | `OK` and `Failed` serialize to exactly those names; parsing accepts any casing (`WIRE-18`) |
| `MODEL-7` | Message-entry equality and hashing | two entries with identical `number`, `message`, `senderid`, and `priority` are equal and hash equally; entries differing in **any one** of the four are unequal. Test each of the four fields differing individually. |
| `MODEL-8` | Message-entry debug/string form | includes all four fields and is stable enough to be useful in logs |
| `MODEL-9` | Request round-trip | serialize a full request object to JSON, deserialize it back, and get an equivalent object — no field lost or altered |

---

## 11. Logging (`LOG-*`)

| ID | Case | Expected |
| --- | --- | --- |
| `LOG-1` | A plain log line | is emitted prefixed with `[CommsSDK]: ` — e.g. `println("hello")` produces `[CommsSDK]: hello` |
| `LOG-2` | A formatted log line with arguments | arguments substitute correctly and the same `[CommsSDK]: ` prefix is applied |
| `LOG-3` | Logging called with null/empty input, or with more/fewer format arguments than placeholders | **never throws** — a logging failure must never interrupt the caller's actual operation |
| `LOG-4` | No log line ever contains the raw API key | in particular, the `SEND-7` failure path logs the outgoing request for debugging; that dump must redact or omit `userdata.password` rather than printing it verbatim. Capture log output during a forced parse failure and assert the API key string does not appear in it. |

---

## 12. Live sandbox smoke tests (`LIVE-*`)

These run against `https://comms-test.pahappa.net/api/v1/json/` only — **never**
`https://comms.egosms.co/api/v1/json/`. All except `LIVE-1` are gated on
`COMMS_SANDBOX_USERNAME` and `COMMS_SANDBOX_API_KEY` per `GATE-1`.

Use reserved test numbers throughout: `256700000000`, `256700000001`, `256700000002`.

| ID | Case | Expected |
| --- | --- | --- |
| `LIVE-1` | Deliberately wrong credentials (e.g. `"invalid-user"` / `"invalid-key-00000000000000000000000000000000"`) — **always runs, unskipped** | a client instance is returned (not null) and its authenticated flag is `false`. Wrong credentials must never authenticate. |
| `LIVE-2` | Authenticate with the real sandbox credentials, then send one SMS to `256700000000` | a non-null result with status `OK` |
| `LIVE-3` | Send to `["256700000000", "256700000001", "256700000002"]` in one call | a non-null result with status `OK` |
| `LIVE-4` | Send to 1001 numbers (generate `256700000000`–`256700001000`) | the API **rejects the batch cleanly**: a non-null result with status `Failed`. No exception, no hang, no partial send. This proves the SDK surfaces server-side limits as ordinary failure values. |
| `LIVE-5` | `queryBalance()` and `getBalance()` against the sandbox | status `OK`, and the balance is `>= 0` |
| `LIVE-6` | Teardown | the API URL is restored to its pre-test value, so no later test unexpectedly runs against the sandbox — or worse, is left pointed somewhere unintended |

---

## 13. Resilience (`RES-*`)

Driven by making the mock server misbehave in ways real backends occasionally do.

| ID | Case | Expected |
| --- | --- | --- |
| `RES-1` | Mock returns `HTTP 500` | surfaced as a handled failure matching that operation's contract above (`false` / "no result" / raised error) — never an unhandled crash |
| `RES-2` | Mock returns `HTTP 401` or `HTTP 403` | same as `RES-1` |
| `RES-3` | Mock returns `HTTP 200` with an empty body | handled like a parse failure (`SEND-7` / `AUTH-9`) |
| `RES-4` | Mock returns a truncated or non-JSON body, e.g. `<html><body>502</body></html>` or `{"Status":` | same as `RES-3` |
| `RES-5` | The mock is stopped before the call (connection refused) | handled per `AUTH-8` / `BAL-9`, with an actionable error message naming the failure |
| `RES-6` | Mock delays its response | the client's timeout behaviour is documented and asserted — or, if no timeout is configured, that absence is explicitly noted and tested as the current deliberate behaviour, so it isn't mistaken for an oversight |
| `RES-7` | A batch of 500 valid numbers in one send call | a **single** request whose `msgdata` array has exactly 500 elements — nothing truncated, nothing silently split into multiple requests |

---

## 14. Coverage checklist per SDK

Copy this table into each SDK's own README or test file and keep it current.

| Suite | Java | Kotlin | JS/TS | Python | Ruby | PHP | Rust | Dart | C# | Go |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `MOCK-*` mock server | | | | | | | | | | |
| `GATE-*` credential gating | | | | | | | | | | |
| `ENV-*` environment | | | | | | | | | | |
| `AUTH-*` authentication | | | | | | | | | | |
| `SENDER-*` sender ID | | | | | | | | | | |
| `NUM-*` number validation | | | | | | | | | | |
| `ARG-*` argument validation | | | | | | | | | | |
| `WIRE-*` wire format | | | | | | | | | | |
| `SEND-*` send behaviour | | | | | | | | | | |
| `BAL-*` balance | | | | | | | | | | |
| `MODEL-*` enums & models | | | | | | | | | | |
| `LOG-*` logging | | | | | | | | | | |
| `LIVE-*` sandbox smoke | | | | | | | | | | |
| `RES-*` resilience | | | | | | | | | | |

Legend: ✅ complete · ⚠️ partial · ❌ missing. Fill in each cell only after auditing that SDK's
actual test files — don't assume a cell is accurate until it's been checked.

---

## 15. Conventions

1. **Never hardcode credentials.** Sandbox credentials come from `COMMS_SANDBOX_USERNAME` and
   `COMMS_SANDBOX_API_KEY`, loaded from a repo-root `.env` by the top-level test runner when
   present. See `.env.example`.
2. **Never let a test hit live production.** Tests reach either
   `https://comms-test.pahappa.net/api/v1/json/` or the loopback mock — never
   `https://comms.egosms.co/api/v1/json/`.
3. **Restore global state.** The API URL is process-global; save it in setup and restore it in
   teardown around every test that changes it.
4. **Assert on the wire, not on a mocked HTTP client.** Serialization bugs are the class of bug
   most likely to slip through when only the client object is stubbed.
5. **Use reserved test numbers** (`256700000000`–`256700001000`) so no real recipient is ever
   messaged.
6. **Skips must be loud.** A skipped credential-gated test states why, e.g. `Skipping live
   sandbox test: set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run it`.
7. **A new test ID here is a task in every SDK.** When behaviour changes, update this file first,
   then each of the ten suites.

---

## 16. Per-language deviations

Record here any test ID a port cannot implement as written, plus the `WIRE-15` choice (absent vs.
explicit-null `msgdata` on Balance) and the `BAL-10` choice (error vs. sentinel), so
cross-port differences stay visible rather than being discovered by a user.

| ID | SDK | Reason | Substitute / choice made |
| --- | --- | --- | --- |
| _(none recorded yet)_ | | | |
