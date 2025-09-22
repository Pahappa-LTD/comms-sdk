# Usage

```python
# install the package
pip install egosms_sdk
# Or for dev "pip install ."


# import in your project
from egosms_sdk.v1 import EgoSmsSDK, MessagePriority

# use
EgoSmsSDK.authenticate("username", "password")
EgoSmsSDK.send_sms("0712345678", "Message to send")
# send_sms(self, numbers: List[str] | str, message: str, sender_id: Optional[str] = None, priority: MessagePriority = MessagePriority.HIGHEST)
```