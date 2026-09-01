use comms_sdk::v1::CommsSDK;
use serial_test::serial;

// CommsSDK::API_URL is a shared global, so any test that reads or writes it
// (directly, via use_sandbox()/use_live_server(), or indirectly by making an
// HTTP call) must not run concurrently with another such test — hence #[serial]
// on every test in this file.

#[test]
#[serial]
fn test_new() {
    let _sdk = CommsSDK::authenticate("username", "api_key").unwrap();
}

#[test]
#[serial]
fn test_authenticate() {
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate("username", "api_key").unwrap();
    assert!(!sdk.is_authenticated()); // not authenticated because that account does not exist on the sandbox
}

#[test]
#[serial]
fn test_send_sms_failure() {
    let sdk = CommsSDK::authenticate("username", "api_key").unwrap();
    let numbers = vec!["256700000000"];
    let message = "Test message";
    let result = sdk.send_sms(numbers, message);
    assert!(result.is_err());
}

#[test]
#[serial]
fn test_send_sms_success() {
    CommsSDK::use_sandbox(); // for testing at https://comms-test.pahappa.net/api/v1/json/
    let sdk = CommsSDK::authenticate("aganidaniel", "466be53e8ba7eea6c6b6f6d3d6e166652ed4e7eebe50bf3b").unwrap(); // replace with appropriate credentials
    let numbers = vec!["256700000000"];
    let message = "Test message from Rust";
    let result = sdk.query_send_sms(numbers, message).unwrap();
    assert!(result.status.is_ok());
}


#[test]
#[serial]
fn test_balance_sandbox() {
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate("aganidaniel", "466be53e8ba7eea6c6b6f6d3d6e166652ed4e7eebe50bf3b").unwrap();
    assert!(sdk.is_authenticated()); // provided credentials were correct
    let result = sdk.get_balance().unwrap();
    assert!(result >= 0.0);
    println!("Balance: {}", result);
}

#[test]
#[serial]
fn test_balance_live() {
    let sdk = CommsSDK::authenticate("username", "api_key").unwrap();
    assert!(!sdk.is_authenticated()); // provided credentials were incorrect
    let result = sdk.get_balance();
    assert!(result.is_err());
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
