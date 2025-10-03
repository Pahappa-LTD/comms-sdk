package v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.RestTemplate
import v1.models.*
import v1.utils.NumberValidator
import v1.utils.Validator

class CommsSDK {
    var userName: String = ""
        private set
    var apiKey: String = ""
        private set
    var isAuthenticated: Boolean = false
        private set
    var senderId: String = "EgoSMS"
        private set

    companion object {
        var API_URL = "http://176.58.101.43:8080/communications/api/v1/json/"
        val OBJECT_MAPPER = ObjectMapper()
        val client = RestTemplate()

        fun authenticate(userName: String, apiKey: String): CommsSDK {
            val commsSDK = CommsSDK()
            commsSDK.userName = userName
            commsSDK.apiKey = apiKey
            Validator.validateCredentials(commsSDK)
            return commsSDK
        }

        /**
         * Uses the sandbox url - useful for testing scenarios.
         * <br></br>
         * Make an account at "[sandbox.egosms.co](http://sandbox.egosms.co)" to use the sandbox.
         * Use [useLiveServer] for the live server.
         */
        fun useSandBox() {
            API_URL = "http://176.58.101.43:8080/communications/api/v1/json"
        }

        /**
         * Uses the live url - useful for actual messaging scenarios.
         * <br></br>
         * Make an account at "[www.egosms.co](http://www.egosms.co)" to use the live server.
         * Use [useSandBox] for the sandbox server.
         */
        fun useLiveServer() {
            API_URL = "http://176.58.101.43:8080/communications/api/v1/json"
        }

    }

    fun setAuthenticated() {
        this.isAuthenticated = true
    }

    fun withSenderId(senderId: String): CommsSDK {
        this.senderId = senderId
        return this
    }

    fun sendSMS(
        number: String,
        message: String,
        senderId: String = this.senderId,
        priority: MessagePriority = MessagePriority.HIGHEST
    ): Boolean {
        return sendSMS(listOf(number), message, senderId, priority)
    }

    fun sendSMS(
        numbers: List<String>,
        message: String,
        senderId: String = this.senderId,
        priority: MessagePriority = MessagePriority.HIGHEST
    ): Boolean {
        val apiResponse = querySendSMS(numbers, message, senderId, priority)
        if (apiResponse == null) {
            println("Failed to get a response from the server.")
            return false
        }
        when (apiResponse.status) {
            ApiResponseCode.OK -> {
                println("SMS sent successfully.")
                println("MessageFollowUpUniqueCode: " + apiResponse.messageFollowUpCode)
                return true
            }

            ApiResponseCode.Failed -> {
                println("Failed: ${apiResponse.message}")
                return false
            }
            else -> throw RuntimeException("Unexpected response status: " + apiResponse.status)
        }
    }

    /** Same as [sendSMS] but returns the full [ApiResponse] object. */
    fun querySendSMS(
        numbers: List<String>,
        message: String,
        senderId: String,
        priority: MessagePriority
    ): ApiResponse? {
        var numbers = numbers
        var senderId = senderId
        if (sdkNotAuthenticated()) return null
        require(!(numbers.isEmpty())) { "Numbers list cannot be empty" }
        require(!(message.isEmpty())) { "Message cannot be empty" }
        require(message.length != 1) { "Message cannot be a single character" }
        if (senderId.trim { it <= ' ' }.isEmpty()) {
            senderId = this.senderId
        }
        if (senderId.length > 11) {
            println("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.")
        }
        numbers = NumberValidator.validateNumbers(numbers)
        if (numbers.isEmpty()) {
            System.err.println("No valid phone numbers provided. Please check inputs.")
            return null
        }
        val apiRequest = ApiRequest()
        apiRequest.method = "SendSms"
        val messageModels: MutableList<MessageModel> = ArrayList()
        for (num in numbers) {
            val messageModel = MessageModel()
            messageModel.number = num
            messageModel.message = message
            messageModel.senderId = senderId
            messageModel.priority = priority
            messageModels.add(messageModel)
        }
        apiRequest.messageData = messageModels
        apiRequest.userdata = UserData(userName, apiKey)
        val res = client.postForEntity(API_URL, apiRequest, String::class.java)

        try {
            return OBJECT_MAPPER.readValue(res.getBody(), ApiResponse::class.java)
        } catch (e: Exception) {
            System.err.println("Failed to send SMS: " + e.message)
            try {
                System.err.println("Request: " + OBJECT_MAPPER.writeValueAsString(apiRequest))
            } catch (_: Exception) {
            }
            return null
        }
    }

    private fun sdkNotAuthenticated(): Boolean {
        if (!isAuthenticated) {
            System.err.println("SDK is not authenticated. Please authenticate before performing actions.")
            System.err.println("Attempting to re-authenticate with provided credentials...")
            return !Validator.validateCredentials(this)
        }
        return false
    }

    /** Same as [getBalance] but returns the full [ApiResponse] object. */
    fun queryBalance(): ApiResponse? {
        if (sdkNotAuthenticated()) {
            return null
        }
        val apiRequest = ApiRequest()
        apiRequest.method = "Balance"
        apiRequest.userdata = UserData(userName, apiKey)
        try {
            val res = client.postForEntity(API_URL, apiRequest, String::class.java)
            val response = OBJECT_MAPPER.readValue(res.getBody(), ApiResponse::class.java)
            return response
        } catch (e: Exception) {
            throw RuntimeException("Failed to get balance: " + e.message, e)
        }
    }

    fun getBalance(): Double? {
        val response = queryBalance()
        return response?.balance
    }

    override fun toString(): String {
        return "SDK(${this.userName} => ${this.apiKey})"
    }
}