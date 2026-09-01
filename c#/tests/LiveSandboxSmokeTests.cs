using Comms;

namespace CommsTests;

/// <summary>
/// Live smoke test against the real sandbox API, to catch API-side drift that mocked
/// tests can't (e.g. the walletType-null-rejection bug fixed earlier). Only runs when
/// COMMS_SANDBOX_USERNAME / COMMS_SANDBOX_API_KEY are set in the environment; skipped
/// (marked inconclusive, not failed) otherwise so the rest of the suite stays green
/// without requiring real credentials.
/// </summary>
[TestClass]
public class LiveSandboxSmokeTests
{
    [TestMethod]
    public async Task SendSms_AgainstSandbox_Succeeds()
    {
        var username = Environment.GetEnvironmentVariable("COMMS_SANDBOX_USERNAME");
        var apiKey = Environment.GetEnvironmentVariable("COMMS_SANDBOX_API_KEY");

        if (string.IsNullOrEmpty(username) || string.IsNullOrEmpty(apiKey))
        {
            Assert.Inconclusive("COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY not set; skipping live sandbox test.");
            return;
        }

        CommsSdk.UseSandBox();
        var sdk = await CommsSdk.Authenticate(username, apiKey);

        Assert.IsTrue(sdk.IsAuthenticated, "Authentication against the sandbox failed.");

        var success = await sdk.SendSms("256700000000", "Golden-path smoke test from C# SDK");

        Assert.IsTrue(success, "SendSms against the sandbox did not report success.");
    }
}
