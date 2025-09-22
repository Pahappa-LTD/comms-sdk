using System;
using System.Text.Json.Serialization;

namespace EgoSms.Models;

public class UserData{
    [JsonPropertyName("username")]
    public string Username { get; set; }
    [JsonPropertyName("password")]
    public string Password { get; set; }

    public UserData(string username, string password)
    {
        Password = password ?? throw new ArgumentNullException(nameof(password));
        Username = username ?? throw new ArgumentNullException(nameof(username));
    }


}
