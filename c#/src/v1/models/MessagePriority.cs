using System.Text.Json.Serialization;

namespace Comms.Models;

public enum MessagePriority
{
    [JsonPropertyName("0")]
    Highest,
    [JsonPropertyName("1")]
    High,
    [JsonPropertyName("2")]
    Medium,
    [JsonPropertyName("3")]
    Low,
    [JsonPropertyName("4")]
    Lowest,
}
