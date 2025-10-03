import sys
from typing import List, Optional
import requests
from .models import ApiRequest, ApiResponse, ApiResponseCode, MessageModel, MessagePriority, UserData
from .utils import NumberValidator, Validator

class CommsSDK:
    API_URL = "http://176.58.101.43:8080/communications/api/v1/json/"

    def __init__(self):
        self._api_key: Optional[str] = None
        self._user_name: Optional[str] = None
        self._sender_id: str = "EgoSMS"
        self._is_authenticated: bool = False
        self._client = requests.Session()

    @property
    def api_key(self) -> Optional[str]:
        return self._api_key

    @property
    def user_name(self) -> Optional[str]:
        return self._user_name

    @property
    def sender_id(self) -> str:
        return self._sender_id

    @property
    def is_authenticated(self) -> bool:
        return self._is_authenticated

    def set_authenticated(self):
        self._is_authenticated = True

    @classmethod
    def authenticate(cls, user_name: str, api_key: str):
        sdk = cls()
        sdk._user_name = user_name
        sdk._api_key = api_key
        Validator.validate_credentials(sdk)
        return sdk

    @staticmethod
    def use_sandbox():
        CommsSDK.API_URL = "http://176.58.101.43:8080/communications/api/v1/json"

    @staticmethod
    def use_live_server():
        CommsSDK.API_URL = "http://176.58.101.43:8080/communications/api/v1/json"

    def with_sender_id(self, sender_id: str):
        self._sender_id = sender_id
        return self

    def send_sms(self, numbers, message: str, sender_id: Optional[str] = None, priority: MessagePriority = MessagePriority.HIGHEST) -> bool:
        if isinstance(numbers, str):
            numbers = [numbers]
        
        api_response = self.query_send_sms(numbers, message, sender_id or self._sender_id, priority)
        
        if api_response is None:
            print("Failed to get a response from the server.")
            return False
        
        if api_response.Status == ApiResponseCode.OK.value:
            print("SMS sent successfully.")
            print(f"MessageFollowUpUniqueCode: {api_response.MsgFollowUpUniqueCode}")
            return True
        elif api_response.Status == ApiResponseCode.FAILED.value:
            print(f"Failed: {api_response.Message}")
            return False
        else:
            raise RuntimeError(f"Unexpected response status: {api_response.Status}")

    def query_send_sms(self, numbers: List[str], message: str, sender_id: str, priority: MessagePriority) -> Optional[ApiResponse]:
        if self._sdk_not_authenticated():
            return None
        
        if not numbers:
            raise ValueError("Numbers list cannot be empty")
        if not message:
            raise ValueError("Message cannot be empty")
        if len(message) == 1:
            raise ValueError("Message cannot be a single character")
        
        if not sender_id or sender_id.strip() == "":
            sender_id = self._sender_id
        if sender_id and len(sender_id) > 11:
            print("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.")
        
        numbers = NumberValidator.validate_numbers(numbers)
        if not numbers:
            print("No valid phone numbers provided. Please check inputs.", file=sys.stderr)
            return None
        
        api_request = ApiRequest(method="SendSms", userdata=UserData(self._user_name, self._api_key))
        message_models = []
        for num in numbers:
            message_model = MessageModel(number=num, message=message, senderid=sender_id, priority=priority.value)
            message_models.append(message_model)
        api_request.msgdata = message_models
        
        try:
            res = self._client.post(CommsSDK.API_URL, json=api_request.to_dict())
            return ApiResponse(**res.json())
        except Exception as e:
            print(f"Failed to send SMS: {e}", file=sys.stderr)
            try:
                print(f"Request: {api_request.__dict__}", file=sys.stderr)
            except Exception:
                pass
            return None

    def _sdk_not_authenticated(self) -> bool:
        if not self._is_authenticated:
            print("SDK is not authenticated. Please authenticate before performing actions.", file=sys.stderr)
            print("Attempting to re-authenticate with provided credentials...", file=sys.stderr)
            return not Validator.validate_credentials(self)
        return False

    def __str__(self) -> str:
        return f"SDK({self._user_name} => {self._api_key})"

    def query_balance(self) -> Optional[ApiResponse]:
        if self._sdk_not_authenticated():
            return None
        
        api_request = ApiRequest(method="Balance", userdata=UserData(self._user_name, self._api_key))
        
        try:
            res = self._client.post(CommsSDK.API_URL, json=api_request.to_dict())
            return ApiResponse(**res.json())
        except Exception as e:
            raise RuntimeError(f"Failed to get balance: {e}") from e

    def get_balance(self) -> Optional[float]:
        response = self.query_balance()
        return float(response.Balance) if response and response.Balance else None
