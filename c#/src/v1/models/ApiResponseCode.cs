using System.Text.Json.Serialization;

namespace EgoSms.Models;

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum ApiResponseCode
{
    OK,
    Failed,
}
