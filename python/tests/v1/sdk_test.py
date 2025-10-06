import unittest
# import sys
from unittest.mock import patch, MagicMock
from io import StringIO
import requests

from comms_sdk.v1 import CommsSDK, MessagePriority
from comms_sdk.v1 import utils
# from comms_sdk.v1.models import ApiResponseCode

class TestCommsSDK(unittest.TestCase):

    def setUp(self):
        # Reset API_URL before each test to ensure consistency
        CommsSDK.use_sandbox()

    @patch('comms_sdk.v1.utils.Validator.validate_credentials')
    def test_authenticate_success(self, mock_validate_credentials):
        mock_validate_credentials.return_value = True
        sdk = CommsSDK.authenticate("test_user", "test_password")
        self.assertIsNotNone(sdk)
        self.assertEqual(sdk.username, "test_user")
        self.assertEqual(sdk.password, "test_password")
        self.assertTrue(sdk.is_authenticated)
        mock_validate_credentials.assert_called_once_with(sdk)

    def test_authenticate_api_key_not_supported(self):
        with self.assertRaises(NotImplementedError) as cm:
            CommsSDK.authenticate_with_api_key("some_api_key")
        self.assertIn("API Key authentication is not supported", str(cm.exception))

    def test_use_sandbox(self):
        CommsSDK.use_sandbox()
        self.assertEqual(CommsSDK.API_URL, "http://sandbox.egosms.co/api/v1/json/")

    def test_use_live_server(self):
        CommsSDK.use_sandbox() # Set to sandbox first
        CommsSDK.use_live_server()
        self.assertEqual(CommsSDK.API_URL, "https://www.egosms.co/api/v1/json/")

    def test_with_sender_id(self):
        sdk = CommsSDK()
        sdk_with_sender = sdk.with_sender_id("NewSender")
        self.assertEqual(sdk_with_sender.sender_id, "NewSender")
        self.assertEqual(sdk.sender_id, "NewSender") # Ensure it modifies the original instance

    @patch('comms_sdk.v1.utils.Validator.validate_credentials')
    @patch('requests.Session.post')
    @patch('comms_sdk.v1.utils.NumberValidator.validate_numbers')
    def test_send_sms_success(self, mock_validate_numbers, mock_post, mock_validate_credentials):
        mock_validate_credentials.return_value = True
        mock_validate_numbers.return_value = ["256771234567"]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"Status": "OK", "MsgFollowUpUniqueCode": "12345"}
        mock_post.return_value = mock_response

        sdk = CommsSDK.authenticate("test_user", "test_password")
        
        with patch('sys.stdout', new=StringIO()) as fake_stdout:
            result = sdk.send_sms(
                numbers=["+256771234567"],
                message="Test message",
                sender_id="MySender",
                priority=MessagePriority.HIGH
            )
            self.assertTrue(result)
            self.assertIn("SMS sent successfully.", fake_stdout.getvalue())
            self.assertIn("MessageFollowUpUniqueCode: 12345", fake_stdout.getvalue())
            mock_post.assert_called_once()
            args, kwargs = mock_post.call_args
            self.assertIn(CommsSDK.API_URL, args)
            print(kwargs['json'])
            self.assertIn("SendSms", kwargs['json']['method'])
            self.assertIn("Test message", kwargs['json']['msgdata'][0]['message'])

    @patch('comms_sdk.v1.utils.Validator.validate_credentials')
    def test_send_sms_not_authenticated(self, mock_validate_credentials):
        mock_validate_credentials.return_value = False # Simulate re-authentication failure
        sdk = CommsSDK() # Not authenticated
        
        with patch('sys.stderr', new=StringIO()) as fake_stderr:
            result = sdk.send_sms(numbers=["+256771234567"], message="Test")
            self.assertFalse(result)
            self.assertIn("SDK is not authenticated.", fake_stderr.getvalue())
            self.assertIn("Attempting to re-authenticate", fake_stderr.getvalue())

    def test_send_sms_invalid_inputs(self):
        sdk = CommsSDK.authenticate("user", "pass") # Authenticate for these tests
        sdk.is_authenticated = True # Force authenticated for input validation tests

        with self.assertRaises(ValueError) as cm:
            sdk.send_sms(numbers=[], message="Test")
        self.assertIn("Numbers list cannot be null or empty", str(cm.exception))

        with self.assertRaises(ValueError) as cm:
            sdk.send_sms(numbers=["+256771234567"], message="")
        self.assertIn("Message cannot be null or empty", str(cm.exception))

        with self.assertRaises(ValueError) as cm:
            sdk.send_sms(numbers=["+256771234567"], message="a")
        self.assertIn("Message cannot be a single character", str(cm.exception))

    @patch('comms_sdk.v1.utils.Validator.validate_credentials')
    @patch('requests.Session.post')
    @patch('comms_sdk.v1.utils.NumberValidator.validate_numbers')
    def test_send_sms_no_valid_numbers(self, mock_validate_numbers, mock_post, mock_validate_credentials):
        mock_validate_credentials.return_value = True
        mock_validate_numbers.return_value = [] # Simulate no valid numbers

        sdk = CommsSDK.authenticate("test_user", "test_password")
        
        with patch('sys.stderr', new=StringIO()) as fake_stderr:
            result = sdk.send_sms(numbers=["invalid"], message="Test")
            self.assertFalse(result)
            self.assertIn("No valid phone numbers provided.", fake_stderr.getvalue())
            mock_post.assert_not_called() # Ensure no API call is made

    @patch('comms_sdk.v1.utils.Validator.validate_credentials')
    @patch('requests.Session.post')
    def test_get_balance_success(self, mock_post, mock_validate_credentials):
        mock_validate_credentials.return_value = True
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"Status": "OK", "Balance": "UGX 10000", "MsgFollowUpUniqueCode": "BALANCE_CODE"}
        mock_post.return_value = mock_response

        sdk = CommsSDK.authenticate("test_user", "test_password")
        
        with patch('sys.stdout', new=StringIO()) as fake_stdout:
            balance = sdk.get_balance()
            self.assertEqual(balance, "UGX 10000")
            self.assertIn("MessageFollowUpUniqueCode: BALANCE_CODE", fake_stdout.getvalue())
            mock_post.assert_called_once()
            args, kwargs = mock_post.call_args
            self.assertIn(CommsSDK.API_URL, args)
            self.assertIn("Balance", kwargs['json']['method'])

    @patch('comms_sdk.v1.utils.Validator.validate_credentials')
    def test_get_balance_not_authenticated(self, mock_validate_credentials):
        mock_validate_credentials.return_value = False
        sdk = CommsSDK()
        
        with patch('sys.stderr', new=StringIO()) as fake_stderr:
            balance = sdk.get_balance()
            self.assertIsNone(balance)
            self.assertIn("SDK is not authenticated.", fake_stderr.getvalue())

    @patch('comms_sdk.v1.utils.Validator.validate_credentials')
    @patch('requests.Session.post')
    def test_get_balance_api_error(self, mock_post, mock_validate_credentials):
        mock_validate_credentials.return_value = True
        mock_post.side_effect = requests.exceptions.RequestException("Balance API error")

        sdk = CommsSDK.authenticate("test_user", "test_password")
        
        with self.assertRaises(RuntimeError) as cm:
            sdk.get_balance()
        self.assertIn("Failed to get balance: Balance API error", str(cm.exception))

if __name__ == '__main__':
    unittest.main()