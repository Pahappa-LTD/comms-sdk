package v1.utils;

import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import v1.CommsSDK
import v1.CommsSDK.Companion.API_URL
import v1.CommsSDK.Companion.OBJECT_MAPPER
import v1.models.ApiRequest
import v1.models.ApiResponse
import v1.models.ApiResponseCode
import v1.models.UserData
import v1.models.WalletType


object Validator {
    fun validateCredentials(sdk: CommsSDK): Boolean {
        if (sdk.apiKey.isEmpty() || sdk.userName.isEmpty()) {
            throw (IllegalArgumentException("API Key and Username must be provided"));
        }
        if (!isValidCredential(sdk)) {
            println("Authentication Failed");
            return false;
        }
        println("Validated using basic auth");
        return true;
    }


    private fun isValidCredential(sdk: CommsSDK): Boolean {
        val client = RestTemplate()
//        client.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
//            println("=== Request ===")
//            println("URI: ${request.uri}")
//            println("Method: ${request.method}")
//            println("Headers: ${request.headers}")
//            println("Body: ${String(body)}")
//            println("================")
//
//            execution.execute(request, body)
//        })
        val apiRequest = ApiRequest()
        apiRequest.method = "Balance"
        apiRequest.userdata = UserData(sdk.userName, sdk.apiKey)
        apiRequest.walletType = WalletType.LOCAL
        try {
            val res: ResponseEntity<String> = client.postForEntity(API_URL, apiRequest)
            val apiResponse: ApiResponse = OBJECT_MAPPER.readValue(res.getBody(), ApiResponse::class.java)

            when(apiResponse.status) {
                ApiResponseCode.OK -> {
                    println("Credentials validated successfully.")
                    return true
                }
                ApiResponseCode.Failed -> {
                    throw Exception(apiResponse.message)
                }
                null -> return false
            }
        } catch (e: Exception) {
            println("Error validating credentials: " + e.message)
            return false
        }
    }
}
