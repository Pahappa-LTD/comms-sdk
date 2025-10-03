package v1.models

import com.fasterxml.jackson.annotation.JsonProperty

class ApiRequest {
    @JsonProperty(required = true)
    var method: String? = null

    @JsonProperty(required = true)
    var userdata: UserData? = null

    @JsonProperty(value = "msgdata")
    var messageData: MutableList<MessageModel>? = null
}
