package v1.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Objects

class MessageModel {
    @JsonProperty(required = true)
    var number: String? = null

    @JsonProperty(required = true)
    var message: String? = null

    @JsonProperty(required = true, value = "senderid")
    var senderId: String? = null
    var priority: MessagePriority? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val messageModel = other as MessageModel
        return this.number == messageModel.number &&
                this.message == messageModel.message &&
                this.senderId == messageModel.senderId &&
                this.priority == messageModel.priority
    }

    override fun hashCode(): Int {
        return Objects.hash(number, message, senderId, priority)
    }


    override fun toString(): kotlin.String {
        val sb = java.lang.StringBuilder()
        sb.append("class MessageModel {\n")
        sb.append("    number: ").append(toIndentedString(number)).append("\n")
        sb.append("    message: ").append(toIndentedString(message)).append("\n")
        sb.append("    senderid: ").append(toIndentedString(senderId)).append("\n")
        sb.append("    priority: ").append(toIndentedString(priority)).append("\n")
        sb.append("}")
        return sb.toString()
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private fun toIndentedString(o: kotlin.Any?): kotlin.String {
        if (o == null) {
            return "null"
        }
        return o.toString().replace("\n", "\n    ")
    }
}

