pub mod models;
pub mod utils;

use anyhow::Error;
use models::{ApiRequest, ApiResponse, MessageModel, MessagePriority, UserData};
use reqwest::blocking::Client;
use utils::{validate_credentials, validate_numbers};

const API_URL: &str = "https://www.egosms.co/api/v1/json/";
const SANDBOX_URL: &str = "http://sandbox.egosms.co/api/v1/json/";

pub struct EgoSmsSDK {
    username: String,
    password: String,
    sender_id: String,
    is_authenticated: bool,
    api_url: String,
    client: Client,
}

impl EgoSmsSDK {
    pub fn new<S: AsRef<str>>(username: S, password: S) -> Self {
        Self {
            username: username.as_ref().to_string(),
            password: password.as_ref().to_string(),
            sender_id: "EgoSms".to_string(),
            is_authenticated: false,
            api_url: API_URL.to_string(),
            client: Client::new(),
        }
    }

    pub fn authenticate(&mut self) -> Result<bool, anyhow::Error> {
        self.is_authenticated = validate_credentials(self)?;
        Ok(self.is_authenticated)
    }

    pub fn with_sender_id(mut self, sender_id: &str) -> Self {
        self.sender_id = sender_id.to_string();
        self
    }

    pub fn use_sandbox(&mut self) {
        self.api_url = SANDBOX_URL.to_string();
    }

    pub fn use_live_server(&mut self) {
        self.api_url = API_URL.to_string();
    }

    pub fn send_sms<S: AsRef<str>, T: ToString>(
        &self,
        numbers: Vec<S>,
        message: T,
    ) -> Result<ApiResponse, anyhow::Error> {
        self.send_sms_full(
            numbers,
            message,
            self.sender_id.as_str(),
            MessagePriority::Highest,
        )
    }

    pub fn send_sms_with_sender_id<S: AsRef<str>, T: ToString>(
        &self,
        numbers: Vec<S>,
        message: T,
        sender_id: &str,
    ) -> Result<ApiResponse, anyhow::Error> {
        let final_sender_id = if sender_id.trim().is_empty() {
            self.sender_id.as_str()
        } else {
            sender_id
        };
        self.send_sms_full(numbers, message, final_sender_id, MessagePriority::Highest)
    }

    pub fn send_sms_with_priority<S: AsRef<str>, T: ToString>(
        &self,
        numbers: Vec<S>,
        message: T,
        priority: MessagePriority,
    ) -> Result<ApiResponse, anyhow::Error> {
        self.send_sms_full(numbers, message, self.sender_id.as_str(), priority)
    }

    pub fn send_sms_full<S: AsRef<str>, T: ToString>(
        &self,
        numbers: Vec<S>,
        message: T,
        sender_id: &str,
        priority: MessagePriority,
    ) -> Result<ApiResponse, anyhow::Error> {
        if !self.is_authenticated {
            return Err(Error::msg(
                "SDK is not authenticated. Please authenticate before performing actions",
            ));
        }

        let validated_numbers = validate_numbers(numbers);

        if sender_id.len() > 11 {
            println!(
                "Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages."
            );
        }

        if validated_numbers.is_empty() {
            return Err(Error::msg(
                "No valid numbers provided. They must be in the format (0|256|+256)712345678",
            ));
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
                username: self.username.clone(),
                password: self.password.clone(),
            },
            message_data: message_models,
        };

        let response = self
            .client
            .post(&self.api_url)
            .json(&api_request)
            .send()
            .unwrap()
            .json::<ApiResponse>()
            .unwrap();

        Ok(response)
    }

    pub fn get_balance(&self) -> Result<ApiResponse, anyhow::Error> {
        if !self.is_authenticated {
            // In a real implementation, we would return a proper error here.
            return Err(Error::msg(
                "SDK is not authenticated. Please authenticate before performing actions",
            ));
        }

        let api_request = ApiRequest {
            method: "Balance".to_string(),
            userdata: UserData {
                username: self.username.clone(),
                password: self.password.clone(),
            },
            message_data: None,
        };

        let response = self
            .client
            .post(&self.api_url)
            .json(&api_request)
            .send()
            .unwrap()
            .json::<ApiResponse>()
            .unwrap();

        Ok(response)
    }
}
