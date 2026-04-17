package com.pahappa.systems.commssdk.v1.utils;

import com.pahappa.systems.commssdk.v1.CommsSDK;
import com.pahappa.systems.commssdk.v1.models.ApiRequest;
import com.pahappa.systems.commssdk.v1.models.ApiResponse;
import com.pahappa.systems.commssdk.v1.models.UserData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.pahappa.systems.commssdk.v1.CommsSDK.API_URL;
import static com.pahappa.systems.commssdk.v1.CommsSDK.OBJECT_MAPPER;
import static com.pahappa.systems.commssdk.v1.utils.Log.println;

public final class Validator {
    public static boolean validateCredentials(CommsSDK sdk) {
        if (sdk == null) {
            throw new IllegalArgumentException("CommsSDK instance cannot be null");
        }
        if (sdk.getApiKey() == null || sdk.getUserName() == null) {
            throw new IllegalArgumentException("Either API Key or Username and Password must be provided");
        }
        if (!isValidCredential(sdk)) {
            println("Authentication failed");
            return false;
        }
        println("Validated using an api key");
        return true;
    }


    private static boolean isValidCredential(CommsSDK sdk) {
        RestTemplate client = new RestTemplate();
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("Balance");
        apiRequest.setUserdata(new UserData(sdk.getUserName(), sdk.getApiKey()));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // force JSON because some edge cases had the media type being sent as XML
            HttpEntity<ApiRequest> entity = new HttpEntity<>(apiRequest, headers);
            ResponseEntity<String> res = client.postForEntity(API_URL, entity, String.class);
            ApiResponse apiResponse = OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
            switch (apiResponse.getStatus()) {
                case OK:
                    println("Credentials validated successfully.");
                    return true;
                case Failed:
                    throw new Exception(apiResponse.getMessage());
                default:
                    return false;
            }
        } catch (Exception e) {
            println("Error validating credentials: " + e.getMessage());
            return false;
        }
    }

}
