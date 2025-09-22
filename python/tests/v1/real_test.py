from egosms_sdk import EgoSmsSDK

EgoSmsSDK.use_sandbox()
sdk = EgoSmsSDK.authenticate("aganisandbox", "SandBox")
sdk.get_balance()
sdk.send_sms("0712345678","Message 1")