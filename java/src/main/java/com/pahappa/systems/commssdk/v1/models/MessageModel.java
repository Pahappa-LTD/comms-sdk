package com.pahappa.systems.egosmssdk.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class MessageModel {
    @JsonProperty(required = true)
    private String number;
    @JsonProperty(required = true)
    private String message;
    @JsonProperty(required = true, value = "senderid")
    private String senderId;
    private MessagePriority priority;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageModel messageModel = (MessageModel) o;
        return Objects.equals(this.number, messageModel.number) &&
                Objects.equals(this.message, messageModel.message) &&
                Objects.equals(this.senderId, messageModel.senderId) &&
                Objects.equals(this.priority, messageModel.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, message, senderId, priority);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MessageModel {\n");
        sb.append("    number: ").append(toIndentedString(number)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    senderid: ").append(toIndentedString(senderId)).append("\n");
        sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

