package v1.models

import com.fasterxml.jackson.annotation.JsonProperty

class ApiResponse {
    @JsonProperty(required = true, value = "Status")
    var status: ApiResponseCode? = null

    @JsonProperty(value = "Message")
    var message: String? = null

    @JsonProperty(value = "Cost")
    var cost: Int? = null

    @JsonProperty(value = "MsgFollowUpUniqueCode")
    var messageFollowUpCode: String? = null

    @JsonProperty(value = "Balance")
    var balance: Double? = null

}
