from comms_sdk import CommsSDK

# CommsSDK.use_sandbox()
username = "sandbox"
apikey = "sandbox35"
sdk = CommsSDK.authenticate(username, apikey)
bal = sdk.get_balance()
print(bal)
sdk.send_sms(["0752345678", "0752345679"], "Message 1")