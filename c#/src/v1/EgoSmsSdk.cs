using EgoSms.Models;
using EgoSms.Utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace EgoSms
{
    public class EgoSmsSdk
    {
        public static string ApiUrl = "https://www.egosms.co/api/v1/json/";
        private static readonly HttpClient Client = new();

        public string? ApiKey { get; private set; }
        public string? Username { get; private set; }
        public string? Password { get; private set; }
        public string SenderId { get; private set; } = "EgoSms";
        public bool IsAuthenticated { get; set; }

        private EgoSmsSdk() { }

        public static Task<EgoSmsSdk> Authenticate(string apiKey)
        {
            throw new NotSupportedException("API Key authentication is not supported in this version. Please use username and password authentication.");
        }

        public static async Task<EgoSmsSdk> Authenticate(string username, string password)
        {
            var sdk = new EgoSmsSdk
            {
                Username = username,
                Password = password
            };
            await Validator.ValidateCredentials(sdk);
            return sdk;
        }

        public static void UseSandBox()
        {
            ApiUrl = "http://sandbox.egosms.co/api/v1/json/";
        }

        public static void UseLiveServer()
        {
            ApiUrl = "https://www.egosms.co/api/v1/json/";
        }

        public EgoSmsSdk WithSenderId(string senderId)
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
            if (await SdkNotAuthenticated()) return false;

            if (numbers == null || !numbers.Any())
                throw new ArgumentException("Numbers list cannot be null or empty");

            if (string.IsNullOrEmpty(message))
                throw new ArgumentException("Message cannot be null or empty");
        
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
                return false;
            }

            var apiRequest = new ApiRequest
            {
                Method = "SendSms",
                MessageData = numbers.Select(num => new MessageModel(num, message, senderId ?? this.SenderId, priority)).ToList(),
                Userdata = new UserData(Username!, Password!),
            
            };

            try
            {
                var json = JsonSerializer.Serialize(apiRequest);
                var content = new StringContent(json, Encoding.UTF8, "application/json");
                var response = await Client.PostAsync(ApiUrl, content);
                response.EnsureSuccessStatusCode();

                var responseBody = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonSerializer.Deserialize<ApiResponse>(responseBody);

                switch (apiResponse?.Status)
                {
                    case ApiResponseCode.OK:
                        Console.WriteLine("SMS sent successfully.");
                        Console.WriteLine($"MessageFollowUpUniqueCode: {apiResponse.MessageFollowUpCode}");
                        return true;
                    case ApiResponseCode.Failed:
                        throw new Exception(apiResponse.Message);
                    default:
                        throw new Exception("Unexpected response status: " + apiResponse?.Status);
                }
            }
            catch (Exception e)
            {
                Console.Error.WriteLine("Failed to send SMS: " + e.Message);
                try
                {
                    Console.Error.WriteLine("Request: " + JsonSerializer.Serialize(apiRequest));
                }
                catch { }
                return false;
            }
        }

        private async Task<bool> SdkNotAuthenticated()
        {
            if (IsAuthenticated) return false;

            Console.Error.WriteLine("SDK is not authenticated. Please authenticate before performing actions.");
            Console.Error.WriteLine("Attempting to re-authenticate with provided credentials...");
            return !await Validator.ValidateCredentials(this);
        }

        public async Task<string?> GetBalance()
        {
            if (await SdkNotAuthenticated()) return null;

            var apiRequest = new ApiRequest
            {
                Method = "Balance",
                Userdata = new UserData(Username!, Password!)
            };

            try
            {
                var json = JsonSerializer.Serialize(apiRequest);
                var content = new StringContent(json, Encoding.UTF8, "application/json");
                var response = await Client.PostAsync(ApiUrl, content);
                response.EnsureSuccessStatusCode();

                var responseBody = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonSerializer.Deserialize<ApiResponse>(responseBody);

                Console.WriteLine($"MessageFollowUpUniqueCode: {apiResponse?.MessageFollowUpCode}");
                return apiResponse?.Balance;
            }
            catch (Exception e)
            {
                throw new Exception("Failed to get balance: " + e.Message, e);
            }
        }
    }

}

