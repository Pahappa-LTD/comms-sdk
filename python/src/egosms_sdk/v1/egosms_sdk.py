import sys
from typing import List, Optional
import requests
from .models import ApiRequest, ApiResponse, ApiResponseCode, MessageModel, MessagePriority, UserData
from .utils import NumberValidator, Validator

class EgoSmsSDK:
    API_URL = "https://www.egosms.co/api/v1/json/"

    def __init__(self):
        self._api_key: Optional[str] = None
        self._username: Optional[str] = None
        self._password: Optional[str] = None
        self._sender_id: str = "EgoSms"
        self._is_authenticated: bool = False
        self._client = requests.Session()

    @property
    def api_key(self) -> Optional[str]:
        return self._api_key

    @property
    def username(self) -> Optional[str]:
        return self._username

    @property
    def password(self) -> Optional[str]:
        return self._password

    @property
    def sender_id(self) -> str:
        return self._sender_id

    @sender_id.setter
    def sender_id(self, value: str):
        self._sender_id = value

    @property
    def is_authenticated(self) -> bool:
        return self._is_authenticated

    @is_authenticated.setter
    def is_authenticated(self, value: bool):
        self._is_authenticated = value

    @classmethod
    def authenticate_with_api_key(cls, api_key: str):
        raise NotImplementedError("API Key authentication is not supported in this version. Please use username and password authentication.")

    @classmethod
    def authenticate(cls, username: str, password: str):
        sdk = cls()
        sdk._username = username
        sdk._password = password
        valid = Validator.validate_credentials(sdk)
        sdk.is_authenticated = valid
        return sdk

    @staticmethod
    def use_sandbox():
        EgoSmsSDK.API_URL = "http://sandbox.egosms.co/api/v1/json/"

    @staticmethod
    def use_live_server():
        EgoSmsSDK.API_URL = "https://www.egosms.co/api/v1/json/"

    def with_sender_id(self, sender_id: str):
        self._sender_id = sender_id
        return self

    def send_sms(self, numbers: List[str] | str, message: str, sender_id: Optional[str] = None, priority: MessagePriority = MessagePriority.HIGHEST) -> bool:
        if self._sdk_not_authenticated():
            return False
        
        if not numbers:
            raise ValueError("Numbers list cannot be null or empty")
        if not message or len(message) == 0:
            raise ValueError("Message cannot be null or empty")
        if len(message) == 1:
            raise ValueError("Message cannot be a single character")
        
        if sender_id is None or sender_id.strip() == "":
            sender_id = self._sender_id
        if sender_id and len(sender_id) > 11:
            print("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.", file=sys.stderr)
        # if priority is None:
        #     priority = MessagePriority.HIGHEST
        
        if type(numbers) is str:
            numbers = [numbers]
        numbers = NumberValidator.validate_numbers(numbers)
        if not numbers:
            print("No valid phone numbers provided. Please check inputs.", file=sys.stderr)
            return False
        
        api_request = ApiRequest(method="SendSms", userdata=UserData(self._username, self._password))
        message_models = []
        for num in numbers:
            message_model = MessageModel(number=num, message=message, senderid=sender_id, priority=priority.value)
            message_models.append(message_model)
        api_request.msgdata = message_models
        
        try:
            res = self._client.post(EgoSmsSDK.API_URL, json=api_request.to_dict())
            # res.raise_for_status()
            
            api_response = ApiResponse(**res.json())
            
            
            if api_response.Status == ApiResponseCode.OK.value:
                print(f"SMS sent successfully. MessageFollowUpUniqueCode: {api_response.MsgFollowUpUniqueCode}")
                return True
            elif api_response.Status == ApiResponseCode.FAILED.value:
                raise Exception(api_response.Message)
            else:
                raise RuntimeError(f"Unexpected response status: {api_response.Status}")
        except requests.exceptions.RequestException as e:
            print(f"Failed to send SMS: {e}", file=sys.stderr)
            try:
                print(f"Request: {api_request.__dict__}", file=sys.stderr)
            except Exception:
                pass
            return False
        except Exception as e:
            print(f"Failed to send SMS: {e}", file=sys.stderr)
            try:
                print(f"Request: {api_request.__dict__}", file=sys.stderr)
            except Exception:
                pass
            return False

    def _sdk_not_authenticated(self) -> bool:
        if not self._is_authenticated:
            print("SDK is not authenticated. Please authenticate before performing actions.", file=sys.stderr)
            print("Attempting to re-authenticate with provided credentials...", file=sys.stderr)
            return not Validator.validate_credentials(self)
        return False

    def get_balance(self) -> Optional[str]:
        if self._sdk_not_authenticated():
            return None
        
        api_request = ApiRequest(method="Balance", userdata=UserData(self._username, self._password).to_dict())
        req = api_request.to_dict()
        
        try:
            res = self._client.post(EgoSmsSDK.API_URL, json=req)
            # res.raise_for_status()
            
            response = ApiResponse(**res.json())
            print(f"Balance: {response.Balance}, MessageFollowUpUniqueCode: {response.MsgFollowUpUniqueCode}")
            return response.Balance
        except requests.exceptions.RequestException as e:
            raise RuntimeError(f"Failed to get balance: {e}") from e
        except Exception as e:
            raise RuntimeError(f"Failed to get balance: {e}") from e
