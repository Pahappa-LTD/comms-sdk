package com.pahappa.systems.commssdk.v1.utils;

import com.pahappa.systems.commssdk.v1.CommsSDK;
import com.pahappa.systems.commssdk.v1.models.ApiRequest;
import com.pahappa.systems.commssdk.v1.models.ApiResponse;
import com.pahappa.systems.commssdk.v1.models.UserData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.pahappa.systems.commssdk.v1.CommsSDK.API_URL;
import static com.pahappa.systems.commssdk.v1.CommsSDK.OBJECT_MAPPER;

public final class Validator {
    public static boolean validateCredentials(CommsSDK sdk) {
        if (sdk == null) {
            throw new IllegalArgumentException("CommsSDK instance cannot be null");
        }
        boolean isApiKey = true;
        if (sdk.getApiKey() == null) {
            if (sdk.getPassword() == null || sdk.getUsername() == null) {
                throw new IllegalArgumentException("Either API Key or Username and Password must be provided");
            } else {
                isApiKey = false;
            }
        }
        if (!isValidCredential(sdk, isApiKey)) {
            System.out.println("                                                      _                    \n" +
                    "  /\\     _|_ |_   _  ._ _|_ o  _  _. _|_ o  _  ._    |_ _. o |  _   _| | | \n" +
                    " /--\\ |_| |_ | | (/_ | | |_ | (_ (_|  |_ | (_) | |   | (_| | | (/_ (_| o o \n" +
                    "                                                                           \n" +
                    "\n");
            return false;
        }
        System.out.println(isApiKey ? "Validated using an api key" : "Validated using basic auth");
        sdk.setAuthenticated(true);
        return true;
    }


    private static boolean isValidCredential(CommsSDK sdk, boolean isApiKey) {
        RestTemplate client = new RestTemplate();
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("Balance");
        apiRequest.setUserdata(new UserData(sdk.getUsername(), sdk.getPassword()));
        try {
            ResponseEntity<String> res = client.postForEntity(API_URL, apiRequest, String.class);
            ApiResponse apiResponse = OBJECT_MAPPER.readValue(res.getBody(), ApiResponse.class);
            switch (apiResponse.getStatus()) {
                case OK:
                    System.out.println("Credentials validated successfully.");
                    return true;
                case Failed:
                    throw new Exception(apiResponse.getMessage());
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Error validating credentials: " + e.getMessage());
            return false;
        }
    }

}
