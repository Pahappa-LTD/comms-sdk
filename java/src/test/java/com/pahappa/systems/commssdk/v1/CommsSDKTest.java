package com.pahappa.systems.commssdk.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pahappa.systems.commssdk.v1.models.ApiResponse;
import com.pahappa.systems.commssdk.v1.models.WalletType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Golden-path regression tests for the SendSms/Balance flow, backed by an in-process
 * HTTP server (see {@link MockCommsApiServer}) rather than a live endpoint.
 * <p>
 * These specifically guard against the walletType regression found this session: the live
 * API rejects an explicit JSON {@code "walletType": null} on Balance requests (though it
 * accepts the field being omitted entirely), which silently broke authenticate(). The SDK
 * now always sends an explicit {@code WalletType.LOCAL} on Balance and SendSms requests, and
 * these tests assert that shape directly off the wire.
 */
public class CommsSDKTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEST_NUMBER = "256700000000";

    private MockCommsApiServer server;
    private String originalApiUrl;

    @Before
    public void setUp() throws Exception {
        originalApiUrl = CommsSDK.API_URL;
        server = new MockCommsApiServer();
        CommsSDK.API_URL = server.url();
    }

    @After
    public void tearDown() {
        server.close();
        CommsSDK.API_URL = originalApiUrl;
    }

    @Test
    public void authenticate_alwaysSendsExplicitLocalWalletType() throws Exception {
        // authenticate() -> Validator.isValidCredential() -> Balance request.
        CommsSDK.authenticate("user", "key");

        JsonNode request = OBJECT_MAPPER.readTree(server.lastCapturedBody());
        assertEquals("Balance", request.get("method").asText());
        assertTrue("walletType must be present on the credential-check request", request.has("walletType"));
        assertFalse("walletType must not be null", request.get("walletType").isNull());
        assertEquals("Local", request.get("walletType").asText());
    }

    @Test
    public void querySendSMS_requestShapeIsCorrect_andWalletTypeIsAlwaysExplicitLocal() throws Exception {
        CommsSDK sdk = CommsSDK.authenticate("user", "key");
        assertTrue(sdk.isAuthenticated());

        sdk.querySendSMS(Arrays.asList(TEST_NUMBER), "Hello world");

        JsonNode request = OBJECT_MAPPER.readTree(server.lastCapturedBody());
        assertEquals("SendSms", request.get("method").asText());

        assertTrue("walletType must be present on SendSms requests", request.has("walletType"));
        assertFalse("walletType must not be null", request.get("walletType").isNull());
        assertEquals("Local", request.get("walletType").asText());

        JsonNode msg = request.get("msgdata").get(0);
        assertEquals(TEST_NUMBER, msg.get("number").asText());
        assertEquals("Hello world", msg.get("message").asText());
        assertEquals("EgoSMS", msg.get("senderid").asText());
        // MessagePriority.HIGH serializes to "1" -- must be the default when unspecified.
        assertEquals("1", msg.get("priority").asText());
    }

    @Test
    public void sendSMS_returnsTrue_onSuccessfulResponse() {
        CommsSDK sdk = CommsSDK.authenticate("user", "key");
        server.sendSmsStatus = "OK";

        boolean result = sdk.sendSMS(TEST_NUMBER, "Hello world");

        assertTrue(result);
    }

    @Test
    public void sendSMS_returnsFalse_onFailedResponse_ratherThanThrowing() {
        CommsSDK sdk = CommsSDK.authenticate("user", "key");
        server.sendSmsStatus = "Failed";
        server.sendSmsMessage = "Insufficient balance";

        boolean result = sdk.sendSMS(TEST_NUMBER, "Hello world");

        assertFalse(result);
    }

    @Test
    public void queryBalance_noArg_defaultsToExplicitLocalWalletType() throws Exception {
        CommsSDK sdk = CommsSDK.authenticate("user", "key");
        server.balanceValue = 42.0;

        ApiResponse response = sdk.queryBalance();

        assertEquals(42.0, response.getBalance(), 0.0001);
        JsonNode request = OBJECT_MAPPER.readTree(server.lastCapturedBody());
        assertEquals("Balance", request.get("method").asText());
        assertEquals("Local", request.get("walletType").asText());
    }

    @Test
    public void queryBalance_explicitInternational_isRespected() throws Exception {
        CommsSDK sdk = CommsSDK.authenticate("user", "key");

        sdk.queryBalance(WalletType.INTERNATIONAL);

        JsonNode request = OBJECT_MAPPER.readTree(server.lastCapturedBody());
        assertEquals("International", request.get("walletType").asText());
    }

    @Test
    public void getBalance_returnsParsedBalanceValue() {
        CommsSDK sdk = CommsSDK.authenticate("user", "key");
        server.balanceValue = 17.75;

        double balance = sdk.getBalance();

        assertEquals(17.75, balance, 0.0001);
    }

    @Test
    public void authenticate_withInvalidCredentials_isNotAuthenticated() {
        server.balanceStatus = "Failed";

        CommsSDK sdk = CommsSDK.authenticate("baduser", "badkey");

        assertFalse(sdk.isAuthenticated());
    }
}
