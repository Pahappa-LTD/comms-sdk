use comms_sdk::v1::CommsSDK;
use serial_test::serial;

// CommsSDK::API_URL is a shared global, so any test that reads or writes it
// (directly, via use_sandbox()/use_live_server(), or indirectly by making an
// HTTP call) must not run concurrently with another such test — hence #[serial]
// on every test in this file.

// Sandbox credentials are never hardcoded here — read from the environment so
// this file is safe to commit and to run without network access. Tests that
// need them skip cleanly (print + early return, not a failure) when unset.
fn sandbox_credentials() -> Option<(String, String)> {
    let username = std::env::var("COMMS_SANDBOX_USERNAME").ok()?;
    let api_key = std::env::var("COMMS_SANDBOX_API_KEY").ok()?;
    Some((username, api_key))
}

// These four tests use obviously-fake credentials against the sandbox — never
// production — and cost nothing, so they always run (no env-var gate needed).

#[test]
#[serial]
fn test_new() {
    CommsSDK::use_sandbox();
    let _sdk = CommsSDK::authenticate("username", "api_key").unwrap();
}

#[test]
#[serial]
fn test_authenticate() {
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate("username", "api_key").unwrap();
    assert!(!sdk.is_authenticated()); // wrong credentials: not authenticated
}

#[test]
#[serial]
fn test_send_sms_failure() {
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate("username", "api_key").unwrap();
    let numbers = vec!["256700000000"];
    let message = "Test message";
    let result = sdk.send_sms(numbers, message);
    assert!(result.is_err()); // wrong credentials: send is rejected
}

#[test]
#[serial]
fn test_balance_live() {
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate("username", "api_key").unwrap();
    assert!(!sdk.is_authenticated()); // wrong credentials: not authenticated
    let result = sdk.get_balance();
    assert!(result.is_err());
}

// The tests below need real sandbox credentials to do anything meaningful, so
// they're gated on COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY and skip
// cleanly (print + early return) when unset.

#[test]
#[serial]
fn test_send_sms_success() {
    let Some((username, api_key)) = sandbox_credentials() else {
        eprintln!(
            "Skipping test_send_sms_success: set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run it"
        );
        return;
    };
    CommsSDK::use_sandbox(); // for testing at https://comms-test.pahappa.net/api/v1/json/
    let sdk = CommsSDK::authenticate(&username, &api_key).unwrap();
    let numbers = vec!["256700000000"];
    let message = "Test message from Rust";
    let result = sdk.query_send_sms(numbers, message).unwrap();
    assert!(result.status.is_ok());
}

#[test]
#[serial]
fn test_send_sms_multiple_numbers_sandbox() {
    let Some((username, api_key)) = sandbox_credentials() else {
        eprintln!(
            "Skipping test_send_sms_multiple_numbers_sandbox: set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run it"
        );
        return;
    };
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate(&username, &api_key).unwrap();
    let numbers = vec!["256700000000", "256700000001", "256700000002"];
    let message = "Test multi-number message from Rust";
    let result = sdk.query_send_sms(numbers, message).unwrap();
    assert!(result.status.is_ok());
}

#[test]
#[serial]
fn test_send_sms_rejects_more_than_1000_numbers() {
    let Some((username, api_key)) = sandbox_credentials() else {
        eprintln!(
            "Skipping test_send_sms_rejects_more_than_1000_numbers: set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run it"
        );
        return;
    };
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate(&username, &api_key).unwrap();

    // 1001 syntactically-valid, unique fake numbers (matches the SDK's
    // ^\+?(0|\d{3})\d{9}$ format: "256" + 9 more digits).
    let owned_numbers: Vec<String> = (0..1001u32)
        .map(|i| format!("256{:09}", 700000000 + i))
        .collect();
    let numbers: Vec<&str> = owned_numbers.iter().map(String::as_str).collect();

    // The real API already rejects requests over 1000 recipients server-side.
    // This just proves the SDK surfaces that rejection cleanly (no panic, no
    // partial/batched sending) rather than crashing.
    let result = sdk.query_send_sms(numbers, "Test message for over-limit rejection");
    match result {
        Ok(response) => assert!(
            !response.status.is_ok(),
            "expected the API to reject a >1000-number request, got OK: {:?}",
            response.message
        ),
        Err(e) => println!("API rejected the >1000-number request via error: {e}"),
    }
}

#[test]
#[serial]
fn test_balance_sandbox() {
    let Some((username, api_key)) = sandbox_credentials() else {
        eprintln!(
            "Skipping test_balance_sandbox: set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run it"
        );
        return;
    };
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate(&username, &api_key).unwrap();
    assert!(sdk.is_authenticated()); // provided credentials were correct
    let result = sdk.get_balance().unwrap();
    assert!(result >= 0.0);
    println!("Balance: {}", result);
}

#[test]
#[serial]
fn test_use_sandbox_and_live() {
    use comms_sdk::v1::API_URL;
    // Test sandbox URL
    CommsSDK::use_sandbox();
    assert_eq!(unsafe{ API_URL }, "https://comms-test.pahappa.net/api/v1/json/");

    // Test live server URL
    CommsSDK::use_live_server();
    assert_eq!(unsafe{ API_URL }, "https://comms.egosms.co/api/v1/json/");
}
