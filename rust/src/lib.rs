pub mod models;
pub mod utils;

pub use anyhow::{Error, Result};
use models::{ApiRequest, ApiResponse, MessageModel, MessagePriority, UserData};
use reqwest::blocking::Client;
use utils::{validate_credentials, validate_numbers};

use crate::models::ApiResponseCode;

pub static mut API_URL: &str = "https://comms.egosms.co/api/v1/json/";

pub struct CommsSDK {
    user_name: String,
    api_key: String,
    sender_id: String,
    is_authenticated: bool,
    client: Client,
}

impl CommsSDK {
    pub fn new<S: AsRef<str>>(user_name: S, api_key: S) -> Self {
        Self {
            user_name: user_name.as_ref().to_string(),
            api_key: api_key.as_ref().to_string(),
            sender_id: "EgoSMS".to_string(),
            is_authenticated: false,
            client: Client::new(),
        }
    }

    /// authenticates and validates credentials
    pub fn authenticate(&mut self) -> Result<bool, anyhow::Error> {
        self.is_authenticated = validate_credentials(self)?;
        Ok(self.is_authenticated)
    }

    pub fn with_sender_id(mut self, sender_id: &str) -> Self {
        self.sender_id = sender_id.to_string();
        self
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

    pub fn set_authenticated(&mut self) {
        self.is_authenticated = true;
    }

    pub fn send_sms<S: AsRef<str>, T: ToString>(
        &mut self,
        numbers: Vec<S>,
        message: T,
    ) -> Result<bool, anyhow::Error> {
        self.send_sms_full(
            numbers,
            message,
            &self.sender_id.clone(),
            MessagePriority::Highest,
        )
    }

    pub fn send_sms_full<S: AsRef<str>, T: ToString>(
        &mut self,
        numbers: Vec<S>,
        message: T,
        sender_id: &str,
        priority: MessagePriority,
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

    /// Same as send_sms but returns the full ApiResponse object.
    pub fn query_send_sms<S: AsRef<str>, T: ToString>(
        &mut self,
        numbers: Vec<S>,
        message: T,
    ) -> Result<ApiResponse> {
        self.query_send_sms_full(
            numbers,
            message,
            &self.sender_id.clone(),
            MessagePriority::Highest,
        )
    }

    /// Same as send_sms but returns the full ApiResponse object.
    pub fn query_send_sms_full<S: AsRef<str>, T: ToString>(
        &mut self,
        numbers: Vec<S>,
        message: T,
        sender_id: &str,
        priority: MessagePriority,
    ) -> Result<ApiResponse> {
        self.ensure_authenticated()?;

        let validated_numbers = validate_numbers(numbers);

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
        };

        match self.client.post(unsafe { API_URL }).json(&api_request).send() {
            Ok(response) => match response.json::<ApiResponse>() {
                Ok(api_response) => Ok(api_response),
                Err(e) => Err(Error::msg(format!("Failed to send SMS: {}", e))),
            },
            Err(e) => Err(Error::msg(format!("Failed to send SMS: {}", e))),
        }
    }

    /// Same as get_balance but returns the full ApiResponse object.
    pub fn query_balance(&mut self) -> Result<ApiResponse> {
        self.ensure_authenticated()?;

        let api_request = ApiRequest {
            method: "Balance".to_string(),
            userdata: UserData {
                username: self.user_name.clone(),
                password: self.api_key.clone(),
            },
            message_data: None,
        };

        match self.client.post(unsafe { API_URL }).json(&api_request).send() {
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

    pub fn get_balance(&mut self) -> Result<f64> {
        let response = self.query_balance();
        match response {
            Ok(api_response) => Ok(api_response.balance.unwrap()),
            Err(e) => Err(Error::msg(format!("Unable to get balance: {e}"))),
        }
    }

    fn ensure_authenticated(&mut self) -> Result<()> {
        if !self.is_authenticated {
            return Err(Error::msg(
                "SDK is not authenticated. Please authenticate before performing actions",
            ));
        }
        Ok(())
    }
}

impl std::fmt::Display for CommsSDK {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "SDK({} => {})", self.user_name, self.api_key)
    }
}
