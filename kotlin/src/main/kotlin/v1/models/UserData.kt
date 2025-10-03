package v1.models

import com.fasterxml.jackson.annotation.JsonProperty

class UserData(userName: String, apiKey: String)
{
    var username: String = userName
        private set
    @JsonProperty("password")
    var apikey: String = apiKey
        private set
}
