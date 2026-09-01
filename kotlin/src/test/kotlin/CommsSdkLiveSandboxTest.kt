import v1.CommsSDK
import v1.models.MessagePriority
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Live smoke test against the real sandbox API — proves an actual customer-shaped
 * send-SMS call still succeeds end-to-end, not just against a mock.
 *
 * Skipped (not failed) unless COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY are set,
 * so the suite stays green for anyone without sandbox credentials. Never hardcode
 * real credentials here.
 */
class CommsSdkLiveSandboxTest {
    @Test
    fun sendSms_succeedsAgainstRealSandbox() {
        val username = System.getenv("COMMS_SANDBOX_USERNAME")
        val apiKey = System.getenv("COMMS_SANDBOX_API_KEY")
        assumeTrue(
            "Skipping live sandbox test: COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY not set",
            !username.isNullOrBlank() && !apiKey.isNullOrBlank()
        )

        CommsSDK.useSandBox()
        val sdk = CommsSDK.authenticate(username!!, apiKey!!)
        assertTrue(sdk.isAuthenticated, "Authentication against sandbox failed")

        val response = sdk.querySendSMS(
            listOf("256700000000"),
            "Golden-path smoke test from Kotlin SDK",
            sdk.senderId,
            MessagePriority.HIGH
        )
        assertTrue(response != null && response.status?.name == "OK", "Expected OK status, got: ${response?.status}")
    }
}
