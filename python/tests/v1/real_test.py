"""
Manual smoke-test script (not part of the automated pytest suite).

Previously this module ran its network calls unconditionally at import time,
which meant pytest's default *_test.py collection was silently executing a
live API call (with placeholder "sandbox"/"sandbox35" credentials) on every
test run. Guarding it behind __main__ stops that; the real, automated
live-sandbox coverage now lives in sdk_test.py's TestLiveSandbox class and
validator_test.py's test_real, both gated on COMMS_SANDBOX_USERNAME /
COMMS_SANDBOX_API_KEY so they skip cleanly when those aren't set.

Run this file directly (`python tests/v1/real_test.py`) for an ad-hoc manual
check against the sandbox.
"""

from comms_sdk import CommsSDK

if __name__ == "__main__":
    username = "sandbox"
    apikey = "sandbox35"
    sdk = CommsSDK.authenticate(username, apikey)
    bal = sdk.get_balance()
    print(bal)
    sdk.send_sms(["0752345678", "0752345679"], "Message 1")
