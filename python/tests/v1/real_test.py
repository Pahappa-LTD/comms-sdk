from comms_sdk import CommsSDK

CommsSDK.use_sandbox()
sdk = CommsSDK.authenticate("agabu-idaniel", "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99")
bal = sdk.get_balance()
print(bal)
sdk.send_sms(["0752345678", "0752345679"], "Message 1")