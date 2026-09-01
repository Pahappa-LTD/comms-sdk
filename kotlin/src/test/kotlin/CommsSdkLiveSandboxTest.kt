import v1.CommsSDK
import v1.models.MessagePriority
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Live tests against the real sandbox API only — never live production.
 *
 * The four credential-gated tests are skipped (not failed) unless
 * COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY are set, so the suite stays
 * green for anyone without sandbox credentials. Never hardcode real
 * credentials here. Every test calls useSandBox() itself so none of them
 * depend on another test's ordering or leftover state.
 */
class CommsSdkLiveSandboxTest {

    @Test
    fun wrongCredentials_areRejected() {
        CommsSDK.useSandBox()
        val sdk = CommsSDK.authenticate("invalid-user", "invalid-key-00000000000000000000000000000000")
        assertFalse(sdk.isAuthenticated, "Expected authentication to fail for made-up credentials")
    }

    @Test
    fun sendSms_succeedsAgainstRealSandbox() {
        val (username, apiKey) = sandboxCredentialsOrSkip()

        CommsSDK.useSandBox()
        val sdk = CommsSDK.authenticate(username, apiKey)
        assertTrue(sdk.isAuthenticated, "Authentication against sandbox failed")

        val response = sdk.querySendSMS(
            listOf("256700000000"),
            "Golden-path smoke test from Kotlin SDK",
            sdk.senderId,
            MessagePriority.HIGH
        )
        assertTrue(response != null && response.status?.name == "OK", "Expected OK status, got: ${response?.status}")
    }

    @Test
    fun sendSms_multipleNumbers_succeedsAgainstRealSandbox() {
        val (username, apiKey) = sandboxCredentialsOrSkip()

        CommsSDK.useSandBox()
        val sdk = CommsSDK.authenticate(username, apiKey)
        assertTrue(sdk.isAuthenticated, "Authentication against sandbox failed")

        val response = sdk.querySendSMS(
            listOf("256700000000", "256700000001", "256700000002"),
            "Golden-path multi-number smoke test from Kotlin SDK",
            sdk.senderId,
            MessagePriority.HIGH
        )
        assertTrue(response != null && response.status?.name == "OK", "Expected OK status, got: ${response?.status}")
    }

    @Test
    fun sendSms_rejectsMoreThan1000Numbers() {
        val (username, apiKey) = sandboxCredentialsOrSkip()

        CommsSDK.useSandBox()
        val sdk = CommsSDK.authenticate(username, apiKey)
        assertTrue(sdk.isAuthenticated, "Authentication against sandbox failed")

        // 1001 syntactically-valid, distinct fake numbers - the real API rejects
        // this server-side; this proves the SDK surfaces that cleanly instead of
        // throwing or attempting to batch-send anything.
        val tooManyNumbers = (0..1000).map { "256700%06d".format(it) }
        assertTrue(tooManyNumbers.size == 1001 && tooManyNumbers.toSet().size == 1001)

        val response = sdk.querySendSMS(
            tooManyNumbers,
            "This should be rejected for having too many recipients",
            sdk.senderId,
            MessagePriority.HIGH
        )
        assertTrue(
            response == null || response.status?.name != "OK",
            "Expected the API to reject a >1000-number request, got OK"
        )
    }

    @Test
    fun balanceMethods_returnValidBalanceFromRealSandbox() {
        val (username, apiKey) = sandboxCredentialsOrSkip()

        CommsSDK.useSandBox()
        val sdk = CommsSDK.authenticate(username, apiKey)
        assertTrue(sdk.isAuthenticated, "Authentication against sandbox failed")

        val balanceResponse = sdk.queryBalance()
        assertTrue(balanceResponse != null && balanceResponse.status?.name == "OK", "Expected OK status, got: ${balanceResponse?.status}")

        val balance = sdk.getBalance()
        assertTrue(balance != null && balance >= 0.0, "Expected a non-negative balance, got: $balance")
    }

    private fun sandboxCredentialsOrSkip(): Pair<String, String> {
        val username = System.getenv("COMMS_SANDBOX_USERNAME")
        val apiKey = System.getenv("COMMS_SANDBOX_API_KEY")
        assumeTrue(
            "Skipping live sandbox test: COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY not set",
            !username.isNullOrBlank() && !apiKey.isNullOrBlank()
        )
        return Pair(username!!, apiKey!!)
    }
}
