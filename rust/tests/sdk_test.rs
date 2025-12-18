use comms_sdk::v1::CommsSDK;

#[test]
fn test_new() {
    let _sdk = CommsSDK::authenticate("username", "api_key");
}

#[test]
fn test_authenticate() {
    CommsSDK::use_sandbox();
    let sdk = CommsSDK::authenticate("username", "api_key");
    assert!(!sdk.is_authenticated()); // not authenticated because that account does not exist on the sandbox
}

#[test]
fn test_send_sms_failure() {
    let mut sdk = CommsSDK::authenticate("username", "api_key");
    let numbers = vec!["256700000000"];
    let message = "Test message";
    let result = sdk.send_sms(numbers, message);
    assert!(result.is_err());
}

#[test]
fn test_send_sms_success() {
    CommsSDK::use_sandbox(); // for testing at https://comms-test.pahappa.net/api/v1/json/
    let mut sdk = CommsSDK::authenticate("username", "api_key"); // replace with appropriate credentials
    let numbers = vec!["256700000000"];
    let message = "Test message from Rust";
    let result = sdk.query_send_sms(numbers, message).unwrap();
    assert!(result.status.is_ok());
}


#[test]
fn test_balance_sandbox() {
    CommsSDK::use_sandbox();
    let mut sdk = CommsSDK::authenticate("username", "api_key");
    assert!(sdk.is_authenticated()); // provided credentials were correct
    let result = sdk.get_balance().unwrap();
    assert!(result >= 0.0);
    println!("Balance: {}", result);
}

#[test]
fn test_balance_live() {
    let mut sdk = CommsSDK::authenticate("username", "api_key");
    assert!(!sdk.is_authenticated()); // provided credentials were incorrect
    let result = sdk.get_balance();
    assert!(result.is_err());
}

#[test]
fn test_use_sandbox_and_live() {
    use comms_sdk::v1::API_URL;
    // Test sandbox URL
    CommsSDK::use_sandbox();
    assert_eq!(unsafe{ API_URL }, "https://comms-test.pahappa.net/api/v1/json/");

    // Test live server URL
    CommsSDK::use_live_server();
    assert_eq!(unsafe{ API_URL }, "https://comms.egosms.co/api/v1/json/");
}
