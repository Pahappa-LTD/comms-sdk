
use serde::{Serialize, Deserialize};

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

#[derive(Serialize, Deserialize, Debug)]
pub struct UserData {
    pub username: String,
    #[serde(rename = "password")]
    pub password: String, // This is actually the API key, but serializes as "password"
}

impl UserData {
    pub fn new(username: &str, api_key: &str) -> Self {
        Self { username: username.into(), password: api_key.into() }
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
}

#[derive(Serialize, Deserialize, Debug)]
pub struct ApiResponse {
    #[serde(rename = "Status")]
    pub status: ApiResponseCode,
    #[serde(rename = "Message")]
    pub message: Option<String>,
    #[serde(rename = "Cost")]
    pub cost: Option<i32>,
    #[serde(rename = "MsgFollowUpUniqueCode")]
    pub message_follow_up_code: Option<String>,
    #[serde(rename = "Balance")]
    pub balance: Option<f64>,
}

