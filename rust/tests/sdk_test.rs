use egosms_sdk::EgoSmsSDK;

#[test]
fn test_new() {
    let _sdk = EgoSmsSDK::new("username", "password");
}

#[test]
fn test_authenticate() {
    let mut sdk = EgoSmsSDK::new("username", "password");
    sdk.use_sandbox();
    let result = sdk.authenticate().unwrap();
    assert!(!result);
}

#[test]
fn test_send_sms_failure() {
    let mut sdk = EgoSmsSDK::new("username", "password");
    sdk.authenticate().unwrap();
    let numbers = vec!["256700000000"];
    let message = "Test message";
    let result = sdk.send_sms(numbers, message);
    assert!(result.is_err());
}

#[test]
fn test_send_sms_success() {
    let mut sdk = EgoSmsSDK::new("aganisandbox", "SandBox");
    sdk.use_sandbox(); // for testing at http://sandbox.egosms.co/api/v1/json/
    sdk.authenticate().unwrap();
    let numbers = vec!["256700000000"];
    let message = "Test message from Rust";
    let result = sdk.send_sms(numbers, message).unwrap();
    assert!(result.status.is_ok());
}


#[test]
fn test_balance_sandbox() {
    let mut sdk = EgoSmsSDK::new("aganisandbox", "SandBox");
    sdk.use_sandbox();
    let auth = sdk.authenticate();
    assert!(auth.is_ok()); // auth request was successful
    assert!(auth.unwrap()); // provided credentials were correct
    let result = sdk.get_balance().unwrap();
    assert!(result.balance.is_some());
    println!("Balance: {}", result.balance.unwrap());
}

#[test]
fn test_balance_live() {
    let mut sdk = EgoSmsSDK::new("live_agani", "live_password");
    let auth = sdk.authenticate();
    assert!(auth.is_ok()); // auth request was successful
    assert!(!auth.unwrap()); // provided credentials were incorrect
    let result = sdk.get_balance();
    assert!(result.is_err());
}