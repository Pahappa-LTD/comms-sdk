package v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import v1.CommsSDK.Companion.useLiveServer
import v1.CommsSDK.Companion.useSandBox
import v1.models.*
import v1.utils.NetworkHelper
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
    internal var apiUrl: String = LIVE_API_URL

    companion object {
        const val LIVE_API_URL = "https://comms.egosms.co/api/v1/json"
        const val SANDBOX_API_URL = "https://comms-test.pahappa.net/api/v1/json"
        val log: Logger = LoggerFactory.getLogger(CommsSDK::class.java)

        val OBJECT_MAPPER = ObjectMapper()
        internal var defaultUrl: String = LIVE_API_URL

        @Deprecated(
            message = "This is discontinued. Use sdkObject.authenticate() instead.",
            replaceWith = ReplaceWith("CommsSDK(userName, apiKey).authenticate()")
        )
        fun authenticate(userName: String, apiKey: String): CommsSDK {
            val commsSDK = CommsSDK(userName, apiKey)
            commsSDK.apiUrl = defaultUrl
            commsSDK.isAuthenticated = Validator.validateCredentials(commsSDK)
            return commsSDK
        }

        /**
         * Uses the sandbox url - useful for testing scenarios.
         * <br></br>
         * Make an account at "[comms-test.pahappa.net](https://comms-test.pahappa.net)" to use the sandbox.
         * Use [useLiveServer] for the live server.
         */
        @Deprecated(message = "This is discontinued. Use sdkObject.authenticateSandbox() instead.")
        fun useSandBox() {
            defaultUrl = SANDBOX_API_URL
        }

        /**
         * Uses the live url - useful for actual messaging scenarios.
         * <br></br>
         * Make an account at "[comms.egosms.co](https://comms.egosms.co)" to use the live server.
         * Use [useSandBox] for the sandbox server.
         */
        @Deprecated(message = "This is discontinued. Use sdkObject.authenticate() instead.")
        fun useLiveServer() {
            defaultUrl = LIVE_API_URL
        }

    }

    constructor(userName: String, apiKey: String) {
        this.userName = userName
        this.apiKey = apiKey
    }

    fun authenticate(): CommsSDK {
        apiUrl = LIVE_API_URL
        isAuthenticated = Validator.validateCredentials(this)
        return this
    }

    fun authenticateSandbox(): CommsSDK {
        apiUrl = SANDBOX_API_URL
        isAuthenticated = Validator.validateCredentials(this)
        return this
    }

    fun withSenderId(senderId: String): CommsSDK {
        this.senderId = senderId
        return this
    }

    fun sendSMS(
        number: String,
        message: String,
        senderId: String = this.senderId,
        priority: MessagePriority = MessagePriority.HIGH
    ): Boolean {
        return sendSMS(listOf(number), message, senderId, priority)
    }

    fun sendSMS(
        numbers: List<String>,
        message: String,
        senderId: String = this.senderId,
        priority: MessagePriority = MessagePriority.HIGH
    ): Boolean {
        val apiResponse = querySendSMS(numbers, message, senderId, priority)
        if (apiResponse == null) {
            log.error("Failed to get a response from the server.")
            return false
        }
        when (apiResponse.status) {
            ApiResponseCode.OK -> {
                log.info("SMS sent successfully.")
                log.info("MessageFollowUpUniqueCode: " + apiResponse.messageFollowUpCode)
                return true
            }

            ApiResponseCode.Failed -> {
                log.error("Failed: {}", apiResponse.message)
                return false
            }

            else -> throw RuntimeException("Unexpected response status: " + apiResponse.status)
        }
    }

    /** Same as [sendSMS] but returns the full [ApiResponse] object. */
    fun querySendSMS(
        number: String,
        message: String,
        senderId: String = this.senderId,
        priority: MessagePriority = MessagePriority.HIGH
    ): ApiResponse? {
        return querySendSMS(listOf(number), message, senderId, priority)
    }

    /** Same as [sendSMS] but returns the full [ApiResponse] object. */
    fun querySendSMS(
        numbers: List<String>,
        message: String,
        senderId: String = this.senderId,
        priority: MessagePriority = MessagePriority.HIGH
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
            log.warn("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.")
        }
        numbers = NumberValidator.validateNumbers(numbers)
        if (numbers.isEmpty()) {
            log.error("No valid phone numbers provided. Please check inputs.")
            return null
        }
        val messageModels: MutableList<MessageModel> = ArrayList()
        for (num in numbers) {
            val messageModel = MessageModel()
            messageModel.number = num
            messageModel.message = message
            messageModel.senderId = senderId
            messageModel.priority = priority
            messageModels.add(messageModel)
        }
        return sendCustomSMS(messageModels)
    }

    fun sendCustomSMS(messages: MutableList<MessageModel>): ApiResponse? {
        if (sdkNotAuthenticated()) return null
        val apiRequest = ApiRequest()
        apiRequest.method = "SendSms"
        apiRequest.messageData = messages
        apiRequest.userdata = UserData(userName, apiKey)
        apiRequest.walletType = WalletType.LOCAL

        try {
            val res = sendRequest(apiRequest)
            return OBJECT_MAPPER.readValue(res, ApiResponse::class.java)
        } catch (e: Exception) {
            log.error("Failed to send SMS: ", e)
            try {
                log.debug("Request: {}", OBJECT_MAPPER.writeValueAsString(apiRequest))
            } catch (_: Exception) {
            }
            return null
        }
    }

    private fun sdkNotAuthenticated(): Boolean {
        if (!isAuthenticated) {
            log.warn("SDK is not authenticated. Please authenticate before performing actions.")
            log.warn("Attempting to re-authenticate with provided credentials...")
            isAuthenticated = Validator.validateCredentials(this);
            return !isAuthenticated;
        }
        return false
    }

    /** Same as [getBalance] but returns the full [ApiResponse] object. */
    fun queryBalance(walletType: WalletType = WalletType.LOCAL): ApiResponse? {
        if (sdkNotAuthenticated()) {
            return null
        }
        val apiRequest = ApiRequest()
        apiRequest.method = "Balance"
        apiRequest.userdata = UserData(userName, apiKey)
        apiRequest.walletType = walletType
        try {
            val res = sendRequest(apiRequest)
            return OBJECT_MAPPER.readValue(res, ApiResponse::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get balance: " + e.message, e)
        }
    }

    private fun sendRequest(apiRequest: ApiRequest): String {
        return NetworkHelper.post(apiRequest, apiUrl)
    }

    fun getBalance(walletType: WalletType = WalletType.LOCAL): Double? {
        val response = queryBalance(walletType)
        return response?.balance
    }

    override fun toString(): String {
        return "SDK(${userName}, ${senderId}, ${apiUrl})"
    }
}
