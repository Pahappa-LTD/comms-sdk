using System.Text.Json.Serialization;

namespace Comms.Models;

public class ApiResponse
{
    [JsonPropertyName("Status")]
    public ApiResponseCode Status { get; set; }

    [JsonPropertyName("Message")]
    public string? Message { get; set; }

    [JsonPropertyName("Cost")]
    public int? Cost { get; set; }

    [JsonPropertyName("Currency")]
    public string? Currency { get; set; }

    [JsonPropertyName("MsgFollowUpUniqueCode")]
    public string? MessageFollowUpCode { get; set; }

    [JsonPropertyName("Balance")]
    public double? Balance { get; set; }
}
