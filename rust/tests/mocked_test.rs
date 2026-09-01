use comms_sdk::v1::{ApiResponseCode, CommsSDK, API_URL};
use mockito::Matcher;
use serial_test::serial;

// These tests point CommsSDK::API_URL at a local mock HTTP server instead of the
// real sandbox, so they run offline and assert the exact shape of the outgoing
// request. This is what would have caught the walletType regression this
// session: the SDK once silently sent `"walletType":null` (or omitted it) on
// Balance/SendSms requests, which the live API rejects. API_URL is a shared
// global (see sdk_test.rs), and this file shares a process with itself across
// tests, so every test here is #[serial] too.

fn point_api_url_at(server: &mockito::Server) {
    let url = format!("{}/api/v1/json/", server.url());
    let leaked: &'static str = Box::leak(url.into_boxed_str());
    unsafe {
        API_URL = leaked;
    }
}

#[test]
#[serial]
fn authenticate_and_send_sms_always_send_wallet_type_local() {
    let mut server = mockito::Server::new();
    point_api_url_at(&server);

    let auth_mock = server
        .mock("POST", "/api/v1/json/")
        .match_body(Matcher::AllOf(vec![
            Matcher::Regex(r#""method":"Balance""#.to_string()),
            Matcher::Regex(r#""walletType":"Local""#.to_string()),
        ]))
        .with_status(200)
        .with_header("content-type", "application/json")
        .with_body(r#"{"Status":"OK","Message":"Success","Balance":100.0}"#)
        .create();

    let sdk = CommsSDK::authenticate("user", "key").expect("authenticate should not error");
    assert!(
        sdk.is_authenticated(),
        "authenticate() must succeed when the mocked Balance check returns OK"
    );
    auth_mock.assert();

    let send_mock = server
        .mock("POST", "/api/v1/json/")
        .match_body(Matcher::AllOf(vec![
            Matcher::Regex(r#""method":"SendSms""#.to_string()),
            Matcher::Regex(r#""walletType":"Local""#.to_string()),
            // priority defaults to MessagePriority::High, which serializes as "1"
            Matcher::Regex(r#""priority":"1""#.to_string()),
        ]))
        .with_status(200)
        .with_header("content-type", "application/json")
        .with_body(r#"{"Status":"OK","Message":"Success","MsgFollowUpUniqueCode":"ABC123","Cost":50.0}"#)
        .create();

    let response = sdk
        .query_send_sms(vec!["256700000000"], "Test message")
        .expect("send_sms should succeed against the mocked OK response");
    assert_eq!(response.status, ApiResponseCode::OK);
    assert_eq!(response.message_follow_up_code.as_deref(), Some("ABC123"));
    assert_eq!(response.cost, Some(50.0));
    send_mock.assert();

    let sent = sdk
        .send_sms(vec!["256700000000"], "Test message")
        .expect("send_sms should not error on a Status:OK response");
    assert!(sent, "send_sms should return true on Status:OK");
}

#[test]
#[serial]
fn send_sms_returns_false_not_error_on_status_failed() {
    let mut server = mockito::Server::new();
    point_api_url_at(&server);

    server
        .mock("POST", "/api/v1/json/")
        .match_body(Matcher::Regex(r#""method":"Balance""#.to_string()))
        .with_status(200)
        .with_header("content-type", "application/json")
        .with_body(r#"{"Status":"OK","Message":"Success","Balance":100.0}"#)
        .create();

    let sdk = CommsSDK::authenticate("user", "key").expect("authenticate should not error");
    assert!(sdk.is_authenticated());

    server
        .mock("POST", "/api/v1/json/")
        .match_body(Matcher::Regex(r#""method":"SendSms""#.to_string()))
        .with_status(200)
        .with_header("content-type", "application/json")
        .with_body(r#"{"Status":"Failed","Message":"Invalid sender id"}"#)
        .create();

    let result = sdk.send_sms(vec!["256700000000"], "Test message");
    assert_eq!(
        result.expect("a Status:Failed response must not be treated as a transport error"),
        false,
        "send_sms should return Ok(false) on Status:Failed, not panic or error"
    );
}

#[test]
#[serial]
fn query_balance_always_sends_wallet_type_local() {
    let mut server = mockito::Server::new();
    point_api_url_at(&server);

    // The credential check inside authenticate() and the explicit get_balance()
    // call below both build their own ApiRequest for method=Balance (in
    // utils.rs and sdk.rs respectively) — both had the same "walletType":null
    // regression once, and produce identical request bodies, so one mock with
    // expect(2) covers both call sites: if either omits walletType, its
    // request body won't match, mockito falls back to an unmatched response,
    // and the SDK call fails to parse it, failing this test.
    let balance_mock = server
        .mock("POST", "/api/v1/json/")
        .match_body(Matcher::AllOf(vec![
            Matcher::Regex(r#""method":"Balance""#.to_string()),
            Matcher::Regex(r#""walletType":"Local""#.to_string()),
        ]))
        .with_status(200)
        .with_header("content-type", "application/json")
        .with_body(r#"{"Status":"OK","Message":"Success","Balance":250.75}"#)
        .expect(2)
        .create();

    let sdk = CommsSDK::authenticate("user", "key").expect("authenticate should not error");
    assert!(sdk.is_authenticated());

    let balance = sdk.get_balance().expect("get_balance should succeed");
    assert_eq!(balance, 250.75);
    balance_mock.assert();
}
