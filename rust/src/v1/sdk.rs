use super::utils::{validate_credentials, validate_numbers};
use crate::models::{
    ApiRequest, ApiResponse, ApiResponseCode, MessageModel, MessagePriority, PhoneNumbers, UserData,
    WalletType,
};
pub use anyhow::{Error, Result};
use reqwest::blocking::Client;
use std::sync::atomic::{AtomicBool, Ordering};

pub static mut API_URL: &str = "https://comms.egosms.co/api/v1/json/";

#[derive(Debug)]
pub struct CommsSDK {
    pub(super) user_name: String,
    pub(super) api_key: String,
    pub(super) sender_id: String,
    is_authenticated: AtomicBool,
    client: Client,
}

impl Clone for CommsSDK {
    fn clone(&self) -> Self {
        Self {
            user_name: self.user_name.clone(),
            api_key: self.api_key.clone(),
            sender_id: self.sender_id.clone(),
            is_authenticated: AtomicBool::new(self.is_authenticated()),
            client: self.client.clone(),
        }
    }
}

impl CommsSDK {
    /// Authenticates and validates credentials.
    ///
    /// Returns `Err` if `user_name` or `api_key` is empty, or if the credential
    /// check itself could not be performed. A network hiccup or a server-side
    /// rejection of the credentials does not error here — check
    /// [`Self::is_authenticated`] on the returned instance for that; the SDK
    /// will still refuse to perform authenticated operations until re-authenticated
    /// with valid credentials.
    pub fn authenticate<S: AsRef<str>>(user_name: S, api_key: S) -> Result<Self> {
        let sdk = Self {
            user_name: user_name.as_ref().to_string(),
            api_key: api_key.as_ref().to_string(),
            sender_id: "EgoSMS".to_string(),
            is_authenticated: AtomicBool::new(false),
            client: Client::new(),
        };
        let authenticated = validate_credentials(&sdk)?;
        sdk.set_authenticated(authenticated);
        Ok(sdk)
    }

    pub fn with_sender_id(mut self, sender_id: &str) -> Self {
        self.sender_id = sender_id.to_string();
        self
    }

    fn set_authenticated(&self, value: bool) {
        self.is_authenticated.store(value, Ordering::Relaxed);
    }

    pub fn use_sandbox() {
        unsafe {
            API_URL = "https://comms-test.pahappa.net/api/v1/json/";
        }
    }

    pub fn use_live_server() {
        unsafe {
            API_URL = "https://comms.egosms.co/api/v1/json/";
        }
    }

    /// Sends an SMS with the default sender ID and priority.
    ///
    /// `numbers` accepts a single number (`&str` or `String`) or several
    /// (`Vec<&str>` or `Vec<String>`).
    pub fn send_sms(
        &self,
        numbers: impl Into<PhoneNumbers>,
        message: impl ToString,
    ) -> Result<bool, anyhow::Error> {
        self.send_sms_full(numbers, message, None, None)
    }

    /// Sends an SMS with an optional custom sender ID and/or priority.
    ///
    /// `numbers` accepts a single number (`&str` or `String`) or several
    /// (`Vec<&str>` or `Vec<String>`). Pass `None` for `sender_id` to use the
    /// instance's default sender ID, or `None` for `priority` to use
    /// `MessagePriority::High` — override just one, both, or neither.
    pub fn send_sms_full(
        &self,
        numbers: impl Into<PhoneNumbers>,
        message: impl ToString,
        sender_id: Option<&str>,
        priority: Option<MessagePriority>,
    ) -> Result<bool, anyhow::Error> {
        let api_response = self.query_send_sms_full(numbers, message, sender_id, priority)?;

        if api_response.status == ApiResponseCode::OK {
            println!("SMS sent successfully.");
            if let Some(code) = api_response.message_follow_up_code {
                println!("MessageFollowUpUniqueCode: {}", code);
            }
            Ok(true)
        } else if api_response.status == ApiResponseCode::Failed {
            if let Some(msg) = api_response.message {
                println!("Failed: {}", msg);
            }
            Ok(false)
        } else {
            Err(Error::msg(format!(
                "Unexpected response status: {:?}",
                api_response.status
            )))
        }
    }

    /// Same as [`Self::send_sms`] but returns the full [`ApiResponse`] object.
    ///
    /// `numbers` accepts a single number (`&str` or `String`) or several
    /// (`Vec<&str>` or `Vec<String>`).
    pub fn query_send_sms(
        &self,
        numbers: impl Into<PhoneNumbers>,
        message: impl ToString,
    ) -> Result<ApiResponse> {
        self.query_send_sms_full(numbers, message, None, None)
    }

    /// Same as [`Self::send_sms_full`] but returns the full [`ApiResponse`] object.
    ///
    /// `numbers` accepts a single number (`&str` or `String`) or several
    /// (`Vec<&str>` or `Vec<String>`). Pass `None` for `sender_id` to use the
    /// instance's default sender ID, or `None` for `priority` to use
    /// `MessagePriority::High` — override just one, both, or neither.
    pub fn query_send_sms_full(
        &self,
        numbers: impl Into<PhoneNumbers>,
        message: impl ToString,
        sender_id: Option<&str>,
        priority: Option<MessagePriority>,
    ) -> Result<ApiResponse> {
        self.ensure_authenticated()?;

        let sender_id = sender_id.unwrap_or(&self.sender_id);
        let priority = priority.unwrap_or(MessagePriority::High);

        let validated_numbers = validate_numbers(numbers.into());

        if sender_id.len() > 11 {
            println!(
                "Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages."
            );
        }

        if validated_numbers.is_empty() {
            return Err(Error::msg(format!(
                "No valid phone numbers provided. Please check inputs."
            )));
        }

        let message_models = Some(
            validated_numbers
                .into_iter()
                .map(|number| MessageModel {
                    number,
                    message: message.to_string(),
                    sender_id: sender_id.to_string(),
                    priority: priority,
                })
                .collect(),
        );

        let api_request = ApiRequest {
            method: "SendSms".to_string(),
            userdata: UserData {
                username: self.user_name.clone(),
                password: self.api_key.clone(),
            },
            message_data: message_models,
            wallet_type: Some(WalletType::default()),
        };

        match self
            .client
            .post(unsafe { API_URL })
            .json(&api_request)
            .send()
        {
            Ok(response) => match response.json::<ApiResponse>() {
                Ok(api_response) => Ok(api_response),
                Err(e) => Err(Error::msg(format!("Failed to send SMS: {}", e))),
            },
            Err(e) => Err(Error::msg(format!("Failed to send SMS: {}", e))),
        }
    }

    /// Same as [`Self::get_balance`] but returns the full ApiResponse object, for the local wallet.
    pub fn query_balance(&self) -> Result<ApiResponse> {
        self.query_balance_full(None)
    }

    /// Same as [`Self::get_balance_full`] but returns the full ApiResponse object.
    ///
    /// Pass `None` for `wallet_type` to query the local wallet (`WalletType::Local`, the API's default).
    pub fn query_balance_full(&self, wallet_type: Option<WalletType>) -> Result<ApiResponse> {
        self.ensure_authenticated()?;

        let api_request = ApiRequest {
            method: "Balance".to_string(),
            userdata: UserData {
                username: self.user_name.clone(),
                password: self.api_key.clone(),
            },
            message_data: None,
            wallet_type: Some(wallet_type.unwrap_or_default()),
        };

        match self
            .client
            .post(unsafe { API_URL })
            .json(&api_request)
            .send()
        {
            Ok(response) => match response.json::<ApiResponse>() {
                // response has reqwest::Error
                Ok(api_response) => Ok(api_response),
                Err(e) => {
                    // but we expect an anyhow::Error
                    eprintln!("Failed to query balance: {}", e);
                    Err(Error::msg(format!("Failed to query balance: {}", e)))
                }
            },
            Err(e) => {
                eprintln!("Failed to query balance: {}", e);
                Err(Error::msg(format!("Failed to query balance: {}", e)))
            }
        }
    }

    /// Gets your current local wallet SMS account balance.
    pub fn get_balance(&self) -> Result<f64> {
        self.get_balance_full(None)
    }

    /// Gets the SMS account balance for the given wallet.
    ///
    /// Pass `None` for `wallet_type` to query the local wallet (`WalletType::Local`, the API's default).
    pub fn get_balance_full(&self, wallet_type: Option<WalletType>) -> Result<f64> {
        let response = self.query_balance_full(wallet_type);
        match response {
            Ok(api_response) => Ok(api_response.balance.unwrap_or_default()),
            Err(e) => Err(Error::msg(format!("Unable to get balance: {e}"))),
        }
    }

    fn ensure_authenticated(&self) -> Result<()> {
        if !self.is_authenticated() {
            println!("SDK is not authenticated. Please authenticate before performing actions.");
            println!("Attempting to re-authenticate with provided credentials...");
            let authenticated = validate_credentials(self)?;
            self.set_authenticated(authenticated);
            if !authenticated {
                return Err(Error::msg(
                    "SDK is not authenticated. Please authenticate before performing actions",
                ));
            }
        }
        Ok(())
    }

    pub fn is_authenticated(&self) -> bool {
        self.is_authenticated.load(Ordering::Relaxed)
    }
}

impl std::fmt::Display for CommsSDK {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "SDK({} => {})", self.user_name, self.api_key)
    }
}
