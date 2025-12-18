# Usage

```python
# install the package
pip install comms-sdk
# Or for dev "pip install ."


# import in your project
from comms_sdk.v1 import CommsSDK, MessagePriority

# use
CommsSDK.authenticate("username", "password")
CommsSDK.send_sms("0712345678", "Message to send")
# send_sms(self, numbers: List[str] | str, message: str, sender_id: Optional[str] = None, priority: MessagePriority = MessagePriority.HIGHEST)
```