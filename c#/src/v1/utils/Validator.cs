using Comms.Models;
using System;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace Comms.Utils;

public static class Validator
{
    public static async Task<bool> ValidateCredentials(CommsSdk sdk)
    {
        if (sdk == null)
        {
            throw new ArgumentNullException(nameof(sdk));
        }

        if ( (string.IsNullOrEmpty(sdk.UserName?.Trim()) || string.IsNullOrEmpty(sdk.ApiKey?.Trim())))
        {
            throw new ArgumentException("Either API Key or Username and Password must be provided");
        }

        if (!await IsValidCredential(sdk))
        {
            Console.WriteLine(@"                                                      _                    
  /\     _|_ |_   _  ._ _|_ o  _  _. _|_ o  _  ._    |_ _. o |  _   _| | | 
 /--\ |_| |_ | | (/_ | | |_ | (_ (_|  |_ | (_) | |   | (_| | | (/_ (_| o o 
                                                                           
");
            return false;
        }

        Console.WriteLine("Validated using an api key");
        sdk.IsAuthenticated = true;
        return true;
    }

    private static async Task<bool> IsValidCredential(CommsSdk sdk)
    {
        using var client = new HttpClient();
        var apiRequest = new ApiRequest
        {
            Method = "Balance",
            Userdata = new UserData(sdk.UserName!, sdk.ApiKey!)
        };

        try
        {
            var json = JsonSerializer.Serialize(apiRequest);
            var content = new StringContent(json, Encoding.UTF8, "application/json");
            var response = await client.PostAsync(CommsSdk.ApiUrl, content);
            response.EnsureSuccessStatusCode();

            var responseBody = await response.Content.ReadAsStringAsync();
            var apiResponse = JsonSerializer.Deserialize<ApiResponse>(responseBody);

            switch (apiResponse?.Status)
            {
                case ApiResponseCode.OK:
                    Console.WriteLine("Credentials validated successfully.");
                    return true;
                case ApiResponseCode.Failed:
                    throw new Exception(apiResponse.Message);
                default:
                    return false;
            }
        }
        catch (Exception e)
        {
            Console.Error.WriteLine($"Error validating credentials: {e.Message}");
            return false;
        }
    }
}