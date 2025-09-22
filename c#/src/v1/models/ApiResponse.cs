using System.Text.Json.Serialization;

namespace EgoSms.Models;

public class ApiResponse
{
    [JsonPropertyName("Status")]
    public ApiResponseCode Status { get; set; }

    [JsonPropertyName("Message")]
    public string? Message { get; set; }

    [JsonPropertyName("Cost")]
    public string? Cost { get; set; }

    [JsonPropertyName("MsgFollowUpUniqueCode")]
    public string? MessageFollowUpCode { get; set; }

    [JsonPropertyName("Balance")]
    public string? Balance { get; set; }
}
