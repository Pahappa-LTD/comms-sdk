package com.pahappa.systems.commssdk.v1.utils;

import com.pahappa.systems.commssdk.v1.CommsSDK;
import com.pahappa.systems.commssdk.v1.models.ApiRequest;
import com.pahappa.systems.commssdk.v1.models.ApiResponse;
import com.pahappa.systems.commssdk.v1.models.UserData;
import com.pahappa.systems.commssdk.v1.models.WalletType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.pahappa.systems.commssdk.v1.CommsSDK.OBJECT_MAPPER;

public final class Validator {
    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    public static boolean validateCredentials(CommsSDK sdk) {
        if (sdk == null) {
            throw new IllegalArgumentException("CommsSDK instance cannot be null");
        }
        if (sdk.getApiKey() == null || sdk.getUserName() == null) {
            throw new IllegalArgumentException("Either API Key or Username and Password must be provided");
        }
        if (!isValidCredential(sdk)) {
            log.error("Authentication failed");
            return false;
        }
        log.info("Validated using an api key");
        return true;
    }

    private static boolean isValidCredential(CommsSDK sdk) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setMethod("Balance");
        apiRequest.setUserdata(new UserData(sdk.getUserName(), sdk.getApiKey()));
        apiRequest.setWalletType(WalletType.LOCAL);
        try {
            String res = NetworkHelper.post(apiRequest, sdk.getApiUrl());
            ApiResponse apiResponse = OBJECT_MAPPER.readValue(res, ApiResponse.class);
            switch (apiResponse.getStatus()) {
                case OK:
                    log.info("Credentials validated successfully.");
                    return true;
                case Failed:
                    throw new Exception(apiResponse.getMessage());
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("Error validating credentials: ", e);
            return false;
        }
    }
}
