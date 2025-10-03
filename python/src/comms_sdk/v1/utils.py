import re
from typing import List, Set
from .models import ApiRequest, ApiResponse, ApiResponseCode, UserData
import requests
import sys

class NumberValidator:
    _regex = r"^\+?(0|\d{3})\d{9}$"

    @staticmethod
    def validate_numbers(numbers: List[str]) -> List[str]:
        if not numbers:
            print("Number list cannot be null or empty", file=sys.stderr)
            return []

        cleansed_numbers: Set[str] = set()
        for number in numbers:
            if not number or not number.strip():
                print(f"Number ({number}) cannot be null or empty!", file=sys.stderr)
                continue
            
            number = number.strip().replace("-", "").replace(" ", "")
            if re.match(NumberValidator._regex, number):
                if number.startswith("0"):
                    number = "256" + number[1:]
                elif number.startswith("+"):
                    number = number[1:]
                cleansed_numbers.add(number)
            else:
                print(f"Number ({number}) is not valid!", file=sys.stderr)
        return list(cleansed_numbers)

class Validator:
    @staticmethod
    def validate_credentials(sdk) -> bool:
        if sdk is None:
            raise ValueError("EgoSmsSDK instance cannot be null")

        is_api_key = True
        if sdk.api_key is None:
            if sdk.password is None or sdk.username is None:
                raise ValueError("Either API Key or Username and Password must be provided")
            else:
                is_api_key = False
        
        if not Validator._is_valid_credential(sdk, is_api_key):
            print("                                                      _                    \n" +
                  "  /\\     _|_ |_   _  ._ _|_ o  _  _. _|_ o  _  ._    |_ _. o |  _   _| | | \n" +
                  " /--\\ |_| |_ | | (/_ | | |_ | (_ (_|  |_ | (_) | |   | (_| | | (/_ (_| o o \n" +
                  "                                                                           \n" +
                  "\n")
            return False
        
        print("Validated using an api key" if is_api_key else "Validated using basic auth")
        sdk.is_authenticated = True
        return True

    @staticmethod
    def _is_valid_credential(sdk, is_api_key: bool) -> bool:
        client = requests.Session()
        api_request = ApiRequest(method="Balance",userdata=UserData(sdk.username, sdk.password))
        req = api_request.to_dict()
        
        try:
            res = client.post(sdk.API_URL, json=req)
            # res.raise_for_status() # Raise an exception for HTTP errors
            
            api_response = ApiResponse(**res.json())
            
            if api_response.Status == ApiResponseCode.OK.value:
                print("Credentials validated successfully.")
                return True
            elif api_response.Status == ApiResponseCode.FAILED.value:
                raise Exception(api_response.Message)
            else:
                return False
        except requests.exceptions.RequestException as e:
            print(f"Error validating credentials: {e}", file=sys.stderr)
            return False
        except Exception as e:
            print(f"Error validating credentials: {e}", file=sys.stderr)
            return False
