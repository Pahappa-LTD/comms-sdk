package v1.utils;

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import v1.CommsSDK
import v1.CommsSDK.Companion.OBJECT_MAPPER
import v1.models.ApiRequest
import v1.models.ApiResponse
import v1.models.ApiResponseCode
import v1.models.UserData
import v1.models.WalletType


object Validator {
    val log: Logger = LoggerFactory.getLogger(Validator::class.java)

    fun validateCredentials(sdk: CommsSDK): Boolean {
        if (sdk.apiKey.isEmpty() || sdk.userName.isEmpty()) {
            throw (IllegalArgumentException("API Key and Username must be provided"));
        }
        if (!isValidCredential(sdk)) {
            log.warn("Authentication Failed");
            return false;
        }
        log.info("Validated using basic auth");
        return true;
    }

    private fun isValidCredential(sdk: CommsSDK): Boolean {
        val apiRequest = ApiRequest()
        apiRequest.method = "Balance"
        apiRequest.userdata = UserData(sdk.userName, sdk.apiKey)
        apiRequest.walletType = WalletType.LOCAL
        try {
            val res: String = NetworkHelper.post(apiRequest, sdk.apiUrl)
            val apiResponse: ApiResponse = OBJECT_MAPPER.readValue(res, ApiResponse::class.java)

            when(apiResponse.status) {
                ApiResponseCode.OK -> {
                    log.info("Credentials validated successfully.")
                    return true
                }
                ApiResponseCode.Failed -> {
                    throw Exception(apiResponse.message)
                }
                null -> return false
            }
        } catch (e: Exception) {
            log.error("Error validating credentials: ", e)
            return false
        }
    }
}
