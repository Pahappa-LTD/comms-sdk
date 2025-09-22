import unittest
import requests, egosms_sdk
from unittest.mock import patch, MagicMock
from io import StringIO

from egosms_sdk.v1.utils import NumberValidator, Validator
from egosms_sdk.v1.models import ApiRequest, ApiResponse, ApiResponseCode, UserData
from egosms_sdk import EgoSmsSDK # Needed for Validator tests

class TestNumberValidator(unittest.TestCase):

    def test_validate_numbers_valid(self):
        numbers = ["+256771234567", "0701234567", "256789012345"]
        expected = ["256771234567", "256701234567", "256789012345"]
        result = NumberValidator.validate_numbers(numbers)
        self.assertCountEqual(result, expected)

    def test_validate_numbers_with_duplicates(self):
        numbers = ["+256771234567", "0771234567", "256771234567"]
        expected = ["256771234567"]
        result = NumberValidator.validate_numbers(numbers)
        self.assertCountEqual(result, expected)

    def test_validate_numbers_invalid_format(self):
        numbers = ["invalid_number", "0771234567", "123"]
        expected = ["256771234567"]
        with patch('sys.stderr', new=StringIO()) as fake_stderr:
            result = NumberValidator.validate_numbers(numbers)
            self.assertIn("Number (invalid_number) is not valid!", fake_stderr.getvalue())
            self.assertIn("Number (123) is not valid!", fake_stderr.getvalue())
        self.assertCountEqual(result, expected)

    def test_validate_numbers_empty_or_none(self):
        with patch('sys.stderr', new=StringIO()) as fake_stderr:
            result_none = NumberValidator.validate_numbers(None)
            self.assertIn("Number list cannot be null or empty", fake_stderr.getvalue())
            self.assertEqual(result_none, [])

            result_empty = NumberValidator.validate_numbers([])
            self.assertIn("Number list cannot be null or empty", fake_stderr.getvalue())
            self.assertEqual(result_empty, [])

            result_empty_string = NumberValidator.validate_numbers(["", "   "])
            self.assertIn("Number () cannot be null or empty!", fake_stderr.getvalue())
            self.assertIn("Number (   ) cannot be null or empty!", fake_stderr.getvalue())
            self.assertEqual(result_empty_string, [])

class TestValidator(unittest.TestCase):

    @patch('requests.Session.post')
    def test_validate_credentials_success(self, mock_post):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"Status": "OK", "Message": "Success"}
        mock_post.return_value = mock_response

        sdk = EgoSmsSDK()
        sdk._username = "test_user"
        sdk._password = "test_password"

        with patch('sys.stdout', new=StringIO()) as fake_stdout:
            result = Validator.validate_credentials(sdk)
            self.assertTrue(result)
            self.assertTrue(sdk.is_authenticated)
            self.assertIn("Credentials validated successfully.", fake_stdout.getvalue())
            self.assertIn("Validated using basic auth", fake_stdout.getvalue())

    @patch('requests.Session.post')
    def test_validate_credentials_failed_api_response(self, mock_post):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"Status": "Failed", "Message": "Invalid credentials"}
        mock_post.return_value = mock_response

        sdk = EgoSmsSDK()
        sdk._username = "test_user"
        sdk._password = "wrong_password"

        with patch('sys.stderr', new=StringIO()) as fake_stderr:
            result = Validator.validate_credentials(sdk)
            self.assertFalse(result)
            self.assertFalse(sdk.is_authenticated)
            self.assertIn("Error validating credentials: Invalid credentials", fake_stderr.getvalue())

    @patch('requests.Session.post')
    def test_validate_credentials_http_error(self, mock_post):
        mock_post.side_effect = requests.exceptions.RequestException("Network error")

        sdk = EgoSmsSDK()
        sdk._username = "test_user"
        sdk._password = "test_password"

        with patch('sys.stderr', new=StringIO()) as fake_stderr:
            result = Validator.validate_credentials(sdk)
            self.assertFalse(result)
            self.assertFalse(sdk.is_authenticated)
            self.assertIn("Error validating credentials: Network error", fake_stderr.getvalue())

    def test_validate_credentials_no_credentials(self):
        sdk = EgoSmsSDK()
        with self.assertRaises(ValueError) as cm:
            Validator.validate_credentials(sdk)
        self.assertIn("Either API Key or Username and Password must be provided", str(cm.exception))
        self.assertFalse(sdk.is_authenticated)

    def test_validate_credentials_sdk_none(self):
        with self.assertRaises(ValueError) as cm:
            Validator.validate_credentials(None)
        self.assertIn("EgoSmsSDK instance cannot be null", str(cm.exception))

    def test_real(self):
        EgoSmsSDK.use_sandbox()
        sdk = EgoSmsSDK.authenticate("aganisandbox", "SandBox")
        self.assertIsNotNone(sdk.get_balance())
        self.assertTrue(sdk.send_sms("0712345678","Message 1"))

if __name__ == '__main__':
    unittest.main()