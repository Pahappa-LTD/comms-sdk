package com.pahappa.systems.commssdk.v1;

import com.pahappa.systems.commssdk.v1.models.ApiResponse;
import com.pahappa.systems.commssdk.v1.models.ApiResponseCode;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end proof that a real customer can actually authenticate and send a message
 * against the real sandbox API -- never live production. Every test explicitly calls
 * {@link CommsSDK#useSandBox()} itself rather than relying on test ordering. The
 * credential-dependent cases are skipped (not failed) unless both COMMS_SANDBOX_USERNAME
 * and COMMS_SANDBOX_API_KEY are set in the environment, so the suite stays green for
 * anyone without sandbox access (CI included); the wrong-credentials case costs nothing
 * and always runs.
 */
public class LiveSandboxSmokeTest {

    private static final String TEST_NUMBER = "256700000000";
    private static final List<String> TEST_NUMBERS = Arrays.asList("256700000000", "256700000001", "256700000002");

    private String originalApiUrl;

    @Before
    public void setUp() {
        originalApiUrl = CommsSDK.API_URL;
    }

    @After
    public void tearDown() {
        CommsSDK.API_URL = originalApiUrl;
    }

    private static void assumeSandboxCredentials() {
        String username = System.getenv("COMMS_SANDBOX_USERNAME");
        String apiKey = System.getenv("COMMS_SANDBOX_API_KEY");
        Assume.assumeTrue(
                "Skipping live sandbox test: set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run it",
                username != null && !username.trim().isEmpty()
                        && apiKey != null && !apiKey.trim().isEmpty());
    }

    @Test
    public void authenticate_withWrongCredentials_isNotAuthenticated() {
        CommsSDK.useSandBox();
        CommsSDK sdk = CommsSDK.authenticate("invalid-user", "invalid-key-00000000000000000000000000000000");
        assertNotNull(sdk);
        assertFalse("Wrong credentials must never authenticate", sdk.isAuthenticated());
    }

    @Test
    public void authenticateAndSendSms_succeedsAgainstRealSandbox() {
        assumeSandboxCredentials();
        String username = System.getenv("COMMS_SANDBOX_USERNAME");
        String apiKey = System.getenv("COMMS_SANDBOX_API_KEY");

        CommsSDK.useSandBox();
        CommsSDK sdk = CommsSDK.authenticate(username, apiKey);
        assertNotNull(sdk);

        ApiResponse response = sdk.querySendSMS(TEST_NUMBER, "Live sandbox smoke test from Java SDK");

        assertNotNull("Expected a response from the sandbox API", response);
        assertEquals(ApiResponseCode.OK, response.getStatus());
    }

    @Test
    public void sendSms_toMultipleNumbers_succeedsAgainstRealSandbox() {
        assumeSandboxCredentials();
        String username = System.getenv("COMMS_SANDBOX_USERNAME");
        String apiKey = System.getenv("COMMS_SANDBOX_API_KEY");

        CommsSDK.useSandBox();
        CommsSDK sdk = CommsSDK.authenticate(username, apiKey);
        assertNotNull(sdk);

        ApiResponse response = sdk.querySendSMS(TEST_NUMBERS, "Live sandbox multi-number test from Java SDK");

        assertNotNull("Expected a response from the sandbox API", response);
        assertEquals(ApiResponseCode.OK, response.getStatus());
    }

    @Test
    public void sendSms_toMoreThan1000Numbers_isRejectedCleanly() {
        assumeSandboxCredentials();
        String username = System.getenv("COMMS_SANDBOX_USERNAME");
        String apiKey = System.getenv("COMMS_SANDBOX_API_KEY");

        CommsSDK.useSandBox();
        CommsSDK sdk = CommsSDK.authenticate(username, apiKey);
        assertNotNull(sdk);

        List<String> tooManyNumbers = new ArrayList<>();
        for (int i = 0; i <= 1000; i++) {
            tooManyNumbers.add(String.format("256700%06d", i));
        }
        assertEquals(1001, tooManyNumbers.size());

        ApiResponse response = sdk.querySendSMS(tooManyNumbers, "This must be rejected by the real API");

        assertNotNull("The API must respond cleanly, not throw, even when rejecting an oversized request", response);
        assertTrue(
                "Expected the real sandbox API to reject a >1000-number request",
                response.getStatus() == ApiResponseCode.Failed);
    }

    @Test
    public void balanceMethods_returnAValidNonNegativeAmount() {
        assumeSandboxCredentials();
        String username = System.getenv("COMMS_SANDBOX_USERNAME");
        String apiKey = System.getenv("COMMS_SANDBOX_API_KEY");

        CommsSDK.useSandBox();
        CommsSDK sdk = CommsSDK.authenticate(username, apiKey);
        assertNotNull(sdk);

        ApiResponse response = sdk.queryBalance();
        assertNotNull("Expected a response from the sandbox API", response);
        assertEquals(ApiResponseCode.OK, response.getStatus());
        assertTrue(sdk.getBalance() >= 0);
    }
}
