from enum import Enum
from dataclasses import dataclass, asdict
from typing import List, Optional
import json

class ApiResponseCode(Enum):
    OK = "OK"
    FAILED = "Failed"

    @classmethod
    def from_json(cls, json_string: str):
        for code in cls:
            if code.value.lower() == json_string.lower():
                return code
        raise ValueError(f"Unknown value: {json_string}")

class MessagePriority(Enum):
    HIGHEST = "0"
    HIGH = "1"
    MEDIUM = "2"
    LOW = "3"
    LOWEST = "4"

    @classmethod
    def from_value(cls, text: str):
        for priority in cls:
            if priority.value == text:
                return priority
        raise ValueError(f"Unknown priority value: {text}")

class JSONSerializable:
    def to_dict(self):
        return asdict(self)
    
    def to_json(self):
        return json.dumps(self.to_dict())

@dataclass
class UserData(JSONSerializable):
    username: str
    password: str

@dataclass
class MessageModel(JSONSerializable):
    number: str
    message: str
    senderid: str
    priority: MessagePriority

@dataclass
class ApiRequest(JSONSerializable):
    method: str
    userdata: UserData
    msgdata: Optional[List[MessageModel]] = None

@dataclass
class ApiResponse(JSONSerializable):
    Status: ApiResponseCode
    Message: Optional[str] = None
    Cost: Optional[str] = None
    MsgFollowUpUniqueCode: Optional[str] = None
    Balance: Optional[str] = None
