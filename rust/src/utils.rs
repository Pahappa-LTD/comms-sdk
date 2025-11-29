use std::{
    collections::HashSet,
    io::{Error, ErrorKind},
};

use regex::Regex;
use reqwest::blocking::Client;

use crate::{
    API_URL, CommsSDK, models::{ApiRequest, ApiResponse, ApiResponseCode, UserData}
};

pub fn validate_numbers<S: AsRef<str>>(numbers: Vec<S>) -> Vec<String> {
    let regex = Regex::new(r"^\+?(0|\d{3})\d{9}$").unwrap();

    if numbers.is_empty() {
        eprintln!("Number list cannot be null or empty");
        return vec![];
    }

    let mut cleansed: HashSet<String> = HashSet::new();
    for number in numbers {
        let number = number.as_ref();
        if number.trim().is_empty() {
            eprintln!("Number ({}) cannot be empty!", number);
            continue;
        }
        let hyphens_or_spaces = Regex::new(r"[-\s]").unwrap();
        let mut number = hyphens_or_spaces.replace(&number, "").to_string();
        if regex.is_match(&number) {
            if number.starts_with("0") {
                number = format!("256{}", number.split_off(1));
            } else if number.starts_with("+") {
                number = number.split_off(1);
            }
            cleansed.insert(number);
        } else {
            eprintln!("Number ({}) is not valid!", number);
        }
    }

    cleansed.into_iter().collect()
}

pub fn validate_credentials(sdk: &mut CommsSDK) -> Result<bool, Error> {
    // let mut is_api_key = true;
    if sdk.api_key.trim().is_empty() || sdk.user_name.trim().is_empty() {
        return Err(Error::new(
            ErrorKind::Other,
            "Username and Password must be provided",
        ));
    }
    let val = is_valid_credential(sdk);
    Ok(val)
}

fn is_valid_credential(sdk: &mut CommsSDK) -> bool {
    let client = Client::new();
    let request = ApiRequest {
        method: "Balance".into(),
        userdata: UserData::new(&sdk.user_name, &sdk.api_key),
        message_data: None,
    };
    return match client.post(unsafe { API_URL }).json(&request).send() {
        Ok(response) => {
            match response.json::<ApiResponse>() {
                Ok(response) => {
                    if response.status == ApiResponseCode::OK {
                        println!("Credentials validated successfully.");
                        return true;
                    }
                }
                Err(e) => {
                    eprintln!("Error validating credentials: {e}");
                }
            }
            false
        }
        Err(e) => {
            eprintln!("Error validating credentials: {e}");
            false
        }
    };
}
