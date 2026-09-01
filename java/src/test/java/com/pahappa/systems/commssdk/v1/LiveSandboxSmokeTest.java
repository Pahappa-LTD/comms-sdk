package com.pahappa.systems.commssdk.v1;

import com.pahappa.systems.commssdk.v1.models.ApiResponse;
import com.pahappa.systems.commssdk.v1.models.ApiResponseCode;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * End-to-end proof that a real customer can actually authenticate and send a message
 * against the real sandbox API -- not a mock. Skipped (not failed) unless both
 * COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY are set in the environment, so the
 * suite stays green for anyone without sandbox access (CI included).
 */
public class LiveSandboxSmokeTest {

    private static final String TEST_NUMBER = "256700000000";

    private String originalApiUrl;

    @Before
    public void setUp() {
        originalApiUrl = CommsSDK.API_URL;
        String username = System.getenv("COMMS_SANDBOX_USERNAME");
        String apiKey = System.getenv("COMMS_SANDBOX_API_KEY");
        Assume.assumeTrue(
                "Skipping live sandbox test: set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run it",
                username != null && !username.trim().isEmpty()
                        && apiKey != null && !apiKey.trim().isEmpty());
    }

    @After
    public void tearDown() {
        CommsSDK.API_URL = originalApiUrl;
    }

    @Test
    public void authenticateAndSendSms_succeedsAgainstRealSandbox() {
        String username = System.getenv("COMMS_SANDBOX_USERNAME");
        String apiKey = System.getenv("COMMS_SANDBOX_API_KEY");

        CommsSDK.useSandBox();
        CommsSDK sdk = CommsSDK.authenticate(username, apiKey);
        assertNotNull(sdk);

        ApiResponse response = sdk.querySendSMS(TEST_NUMBER, "Live sandbox smoke test from Java SDK");

        assertNotNull("Expected a response from the sandbox API", response);
        assertEquals(ApiResponseCode.OK, response.getStatus());
    }
}
