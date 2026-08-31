use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug, Copy, Clone, PartialEq, Eq)]
pub enum ApiResponseCode {
    OK,
    Failed,
}

impl ApiResponseCode {
    pub fn is_ok(&self) -> bool {
        self == &ApiResponseCode::OK
    }

    pub fn is_failed(&self) -> bool {
        self == &ApiResponseCode::Failed
    }
}

#[derive(Serialize, Deserialize, Debug, Copy, Clone)]
pub enum MessagePriority {
    #[serde(rename = "0")]
    Highest,
    #[serde(rename = "1")]
    High,
    #[serde(rename = "2")]
    Medium,
    #[serde(rename = "3")]
    Low,
    #[serde(rename = "4")]
    Lowest,
}

/// Which wallet to query a balance from. Defaults to `Local` on the API if omitted.
#[derive(Serialize, Deserialize, Debug, Default, Copy, Clone, PartialEq, Eq)]
pub enum WalletType {
    #[default]
    Local,
    International,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct UserData {
    pub username: String,
    #[serde(rename = "password")]
    pub password: String, // This is actually the API key, but serializes as "password"
}

impl UserData {
    pub fn new(username: &str, api_key: &str) -> Self {
        Self {
            username: username.into(),
            password: api_key.into(),
        }
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub struct MessageModel {
    #[serde(rename = "number")]
    pub number: String,
    #[serde(rename = "message")]
    pub message: String,
    #[serde(rename = "senderid")]
    pub sender_id: String,
    #[serde(rename = "priority")]
    pub priority: MessagePriority,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct ApiRequest {
    pub method: String,
    pub userdata: UserData,
    #[serde(rename = "msgdata")]
    pub message_data: Option<Vec<MessageModel>>,
    #[serde(rename = "walletType")]
    pub wallet_type: Option<WalletType>,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct ApiResponse {
    #[serde(rename = "Status")]
    pub status: ApiResponseCode,
    #[serde(rename = "Message")]
    pub message: Option<String>,
    #[serde(rename = "Cost")]
    pub cost: Option<i32>,
    #[serde(rename = "Currency")]
    pub currency: Option<String>,
    #[serde(rename = "MsgFollowUpUniqueCode")]
    pub message_follow_up_code: Option<String>,
    #[serde(rename = "Balance")]
    pub balance: Option<f64>,
}

/// Accepts one or more recipient phone numbers for the SMS-sending methods on [`crate::CommsSDK`].
///
/// This type has no public constructor — pass a `&str`, `String`, `Vec<&str>`, or `Vec<String>`
/// directly wherever a function asks for `impl Into<PhoneNumbers>`, and it will be converted
/// automatically. There is no need to name or build a `PhoneNumbers` value yourself.
pub struct PhoneNumbers(pub(crate) Vec<String>);

mod impls {
    use crate::models::PhoneNumbers;

    impl From<&str> for PhoneNumbers {
        fn from(number: &str) -> Self {
            PhoneNumbers(vec![number.to_string()])
        }
    }

    impl From<String> for PhoneNumbers {
        fn from(number: String) -> Self {
            PhoneNumbers(vec![number])
        }
    }

    impl From<Vec<&str>> for PhoneNumbers {
        fn from(numbers: Vec<&str>) -> Self {
            PhoneNumbers(numbers.iter().map(|s| s.to_string()).collect())
        }
    }

    impl From<Vec<String>> for PhoneNumbers {
        fn from(numbers: Vec<String>) -> Self {
            PhoneNumbers(numbers)
        }
    }
}
