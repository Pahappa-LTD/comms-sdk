using System.Text.Json.Serialization;

namespace Comms.Models;

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum ApiResponseCode
{
    OK,
    Failed,
}
