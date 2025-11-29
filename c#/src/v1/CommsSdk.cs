using Comms.Models;
using Comms.Utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace Comms
{
    public class CommsSdk
    {
        public static string ApiUrl { get; private set; } = "https://comms.egosms.co/api/v1/json/";
        private static readonly HttpClient Client = new();

        public string? UserName { get; private set; }
        public string? ApiKey { get; private set; }
        public string SenderId { get; private set; } = "EgoSMS";
        public bool IsAuthenticated { get; set; }

        private CommsSdk() { }

        public static async Task<CommsSdk> Authenticate(string userName, string apiKey)
        {
            var sdk = new CommsSdk
            {
                UserName = userName,
                ApiKey = apiKey
            };
            await Validator.ValidateCredentials(sdk);
            return sdk;
        }

        public static void UseSandBox()
        {
            ApiUrl = "https://comms-test.pahappa.net/api/v1/json";
        }

        public static void UseLiveServer()
        {
            ApiUrl = "https://comms.egosms.co/api/v1/json";
        }

        public void SetAuthenticated()
        {
            IsAuthenticated = true;
        }

        public CommsSdk WithSenderId(string senderId)
        {
            SenderId = senderId;
            return this;
        }

        public Task<bool> SendSms(string number, string message) =>
            SendSms(new List<string> { number }, message, SenderId, MessagePriority.Highest);

        public Task<bool> SendSms(string number, string message, string senderId) =>
            SendSms(new List<string> { number }, message, senderId, MessagePriority.Highest);

        public Task<bool> SendSms(string number, string message, string senderId, MessagePriority priority) =>
            SendSms(new List<string> { number }, message, senderId, priority);

        public Task<bool> SendSms(string number, string message, MessagePriority priority) =>
            SendSms(new List<string> { number }, message, SenderId, priority);

        public Task<bool> SendSms(List<string> numbers, string message) =>
            SendSms(numbers, message, SenderId, MessagePriority.Highest);

        public Task<bool> SendSms(List<string> numbers, string message, string senderId) =>
            SendSms(numbers, message, senderId, MessagePriority.Highest);

        public Task<bool> SendSms(List<string> numbers, string message, MessagePriority priority) =>
            SendSms(numbers, message, SenderId, priority);

        public async Task<bool> SendSms(List<string> numbers, string message, string senderId, MessagePriority priority)
        {
            var apiResponse = await QuerySendSms(numbers, message, senderId, priority);
            if (apiResponse == null)
            {
                Console.WriteLine("Failed to get a response from the server.");
                return false;
            }

            switch (apiResponse.Status)
            {
                case ApiResponseCode.OK:
                    Console.WriteLine("SMS sent successfully.");
                    Console.WriteLine($"MessageFollowUpUniqueCode: {apiResponse.MessageFollowUpCode}");
                    return true;
                case ApiResponseCode.Failed:
                    Console.WriteLine($"Failed: {apiResponse.Message}");
                    return false;
                default:
                    throw new Exception("Unexpected response status: " + apiResponse.Status);
            }
        }

        /// <summary>Same as <see cref="SendSms"/> but returns the full <see cref="ApiResponse"/> object.</summary>
        public async Task<ApiResponse?> QuerySendSms(List<string> numbers, string message, string senderId, MessagePriority priority)
        {
            if (await SdkNotAuthenticated()) return null;

            if (numbers == null || !numbers.Any())
                throw new ArgumentException("Numbers list cannot be empty");

            if (string.IsNullOrEmpty(message))
                throw new ArgumentException("Message cannot be empty");

            if (message.Length == 1)
                throw new ArgumentException("Message cannot be a single character");

            if (string.IsNullOrWhiteSpace(senderId))
                senderId = SenderId;

            if (senderId?.Length > 11)
                Console.WriteLine("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.");

            numbers = NumberValidator.ValidateNumbers(numbers);
            if (!numbers.Any())
            {
                Console.Error.WriteLine("No valid phone numbers provided. Please check inputs.");
                return null;
            }

            var apiRequest = new ApiRequest
            {
                Method = "SendSms",
                MessageData = numbers.Select(num => new MessageModel(num, message, senderId ?? this.SenderId, priority)).ToList(),
                Userdata = new UserData(UserName!, ApiKey!),
            };

            try
            {
                var json = JsonSerializer.Serialize(apiRequest);
                var content = new StringContent(json, Encoding.UTF8, "application/json");
                var response = await Client.PostAsync(ApiUrl, content);
                response.EnsureSuccessStatusCode();

                var responseBody = await response.Content.ReadAsStringAsync();
                return JsonSerializer.Deserialize<ApiResponse>(responseBody);
            }
            catch (Exception e)
            {
                Console.Error.WriteLine("Failed to send SMS: " + e.Message);
                try
                {
                    Console.Error.WriteLine("Request: " + JsonSerializer.Serialize(apiRequest));
                }
                catch { }
                return null;
            }
        }

        private async Task<bool> SdkNotAuthenticated()
        {
            if (IsAuthenticated) return false;

            Console.Error.WriteLine("SDK is not authenticated. Please authenticate before performing actions.");
            Console.Error.WriteLine("Attempting to re-authenticate with provided credentials...");
            return !await Validator.ValidateCredentials(this);
        }

        /// <summary>Same as <see cref="GetBalance"/> but returns the full <see cref="ApiResponse"/> object.</summary>
        public async Task<ApiResponse?> QueryBalance()
        {
            if (await SdkNotAuthenticated()) return null;

            var apiRequest = new ApiRequest
            {
                Method = "Balance",
                Userdata = new UserData(UserName!, ApiKey!)
            };

            try
            {
                var json = JsonSerializer.Serialize(apiRequest);
                var content = new StringContent(json, Encoding.UTF8, "application/json");
                var response = await Client.PostAsync(ApiUrl, content);
                response.EnsureSuccessStatusCode();

                var responseBody = await response.Content.ReadAsStringAsync();
                return JsonSerializer.Deserialize<ApiResponse>(responseBody);
            }
            catch (Exception e)
            {
                throw new Exception("Failed to get balance: " + e.Message, e);
            }
        }

        public async Task<double?> GetBalance()
        {
            var response = await QueryBalance();
            return response?.Balance;
        }

        public override string ToString()
        {
            return $"SDK({UserName} => {ApiKey})";
        }
    }

}
