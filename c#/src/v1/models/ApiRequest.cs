using System.Text.Json.Serialization;
using System.Collections.Generic;

namespace EgoSms.Models;

public class ApiRequest
{
    [JsonPropertyName("method")] 
    public string Method { get; set; }
    [JsonPropertyName("userdata")] 
    public UserData Userdata { get; set; }
    [JsonPropertyName("msgdata")] 
    public List<MessageModel>? MessageData{ get; set; }
}
