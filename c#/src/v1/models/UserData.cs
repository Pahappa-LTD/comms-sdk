using System;
using System.Text.Json.Serialization;

namespace Comms.Models;

public class UserData{
    [JsonPropertyName("username")]
    public string Username { get; set; }
    [JsonPropertyName("password")]
    public string ApiKey { get; set; }

    public UserData(string userName, string apiKey)
    {
        Username = userName ?? throw new ArgumentNullException(nameof(userName));
        ApiKey = apiKey ?? throw new ArgumentNullException(nameof(apiKey));
    }
}
