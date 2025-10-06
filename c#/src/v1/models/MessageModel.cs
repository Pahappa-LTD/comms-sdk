using System;
using System.Text.Json.Serialization;

namespace Comms.Models;

public class MessageModel
{
    [JsonPropertyName("number")]
    public string Number { get; set; }
    [JsonPropertyName("message")]
    public string Message { get; set; }
    [JsonPropertyName("senderid")]
    public string SenderId { get; set; }
    [JsonPropertyName("priority")]
    public MessagePriority Priority { get; set; } = MessagePriority.Highest;

    public MessageModel(string number, string message, string senderId, MessagePriority priority)
    {
        Number = number ?? throw new ArgumentNullException(nameof(number));
        Message = message ?? throw new ArgumentNullException(nameof(message));
        SenderId = senderId ?? "Comms";
        Priority = priority;
    }
}
