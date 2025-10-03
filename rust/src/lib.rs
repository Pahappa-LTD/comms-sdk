pub mod models;
pub mod utils;

use anyhow::Error;
use models::{ApiRequest, ApiResponse, MessageModel, MessagePriority, UserData};
use reqwest::blocking::Client;
use utils::{validate_credentials, validate_numbers};

const API_URL: &str = "http://176.58.101.43:8080/communications/api/v1/json/";

pub struct CommsSDK {
    user_name: String,
    api_key: String,
    sender_id: String,
    is_authenticated: bool,
    api_url: String,
    client: Client,
}

impl CommsSDK {
    pub fn authenticate<S: AsRef<str>>(user_name: S, api_key: S) -> Result<Self, anyhow::Error> {
        let mut sdk = Self {
            user_name: user_name.as_ref().to_string(),
            api_key: api_key.as_ref().to_string(),
            sender_id: "EgoSMS".to_string(),
            is_authenticated: false,
            api_url: API_URL.to_string(),
            client: Client::new(),
        };
        sdk.is_authenticated = validate_credentials(&sdk)?;
        Ok(sdk)
    }

    pub fn with_sender_id(mut self, sender_id: &str) -> Self {
        self.sender_id = sender_id.to_string();
        self
    }

    pub fn use_sandbox(&mut self) {
        self.api_url = "http://176.58.101.43:8080/communications/api/v1/json".to_string();
    }

    pub fn use_live_server(&mut self) {
        self.api_url = "http://176.58.101.43:8080/communications/api/v1/json".to_string();
    }

    pub fn set_authenticated(&mut self) {
        self.is_authenticated = true;
    }

    pub fn send_sms<S: AsRef<str>, T: ToString>(
        &self,
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
        &self,
        numbers: Vec<S>,
        message: T,
        sender_id: &str,
        priority: MessagePriority,
    ) -> Result<bool, anyhow::Error> {
        let api_response = self.query_send_sms(numbers, message, sender_id, priority)?;
        
        match api_response {
            Some(response) => {
                if response.status.to_lowercase() == "ok" {
                    println!("SMS sent successfully.");
                    if let Some(code) = response.message_follow_up_code {
                        println!("MessageFollowUpUniqueCode: {}", code);
                    }
                    Ok(true)
                } else if response.status.to_lowercase() == "failed" {
                    if let Some(msg) = response.message {
                        println!("Failed: {}", msg);
                    }
                    Ok(false)
                } else {
                    Err(Error::msg(format!("Unexpected response status: {}", response.status)))
                }
            }
            None => {
                println!("Failed to get a response from the server.");
                Ok(false)
            }
        }
    }

    /// Same as send_sms but returns the full ApiResponse object.
    pub fn query_send_sms<S: AsRef<str>, T: ToString>(
        &self,
        numbers: Vec<S>,
        message: T,
        sender_id: &str,
        priority: MessagePriority,
    ) -> Result<Option<ApiResponse>, anyhow::Error> {
        if !self.is_authenticated {
            eprintln!("SDK is not authenticated. Please authenticate before performing actions.");
            eprintln!("Attempting to re-authenticate with provided credentials...");
            return Ok(None);
        }

        let validated_numbers = validate_numbers(numbers);

        if sender_id.len() > 11 {
            println!(
                "Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages."
            );
        }

        if validated_numbers.is_empty() {
            eprintln!("No valid phone numbers provided. Please check inputs.");
            return Ok(None);
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

        match self
            .client
            .post(&self.api_url)
            .json(&api_request)
            .send()
        {
            Ok(response) => match response.json::<ApiResponse>() {
                Ok(api_response) => Ok(Some(api_response)),
                Err(e) => {
                    eprintln!("Failed to send SMS: {}", e);
                    Ok(None)
                }
            },
            Err(e) => {
                eprintln!("Failed to send SMS: {}", e);
                Ok(None)
            }
        }
    }

    /// Same as get_balance but returns the full ApiResponse object.
    pub fn query_balance(&self) -> Result<Option<ApiResponse>, anyhow::Error> {
        if !self.is_authenticated {
            eprintln!("SDK is not authenticated. Please authenticate before performing actions.");
            eprintln!("Attempting to re-authenticate with provided credentials...");
            return Ok(None);
        }

        let api_request = ApiRequest {
            method: "Balance".to_string(),
            userdata: UserData {
                username: self.user_name.clone(),
                password: self.api_key.clone(),
            },
            message_data: None,
        };

        match self
            .client
            .post(&self.api_url)
            .json(&api_request)
            .send()
        {
            Ok(response) => match response.json::<ApiResponse>() {
                Ok(api_response) => Ok(Some(api_response)),
                Err(e) => Err(Error::msg(format!("Failed to get balance: {}", e))),
            },
            Err(e) => Err(Error::msg(format!("Failed to get balance: {}", e))),
        }
    }

    pub fn get_balance(&self) -> Result<Option<f64>, anyhow::Error> {
        let response = self.query_balance()?;
        match response {
            Some(api_response) => {
                if let Some(balance_str) = api_response.balance {
                    Ok(balance_str.parse::<f64>().ok())
                } else {
                    Ok(None)
                }
            }
            None => Ok(None),
        }
    }
}

impl std::fmt::Display for CommsSDK {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "SDK({} => {})", self.user_name, self.api_key)
    }
}
}
