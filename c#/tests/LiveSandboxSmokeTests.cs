using Comms;
using Comms.Models;

namespace CommsTests;

/// <summary>
/// Live smoke tests against the real sandbox API only (never production), to catch
/// API-side drift that mocked tests can't (e.g. the walletType-null-rejection bug
/// fixed earlier). The credential-dependent cases only run when
/// COMMS_SANDBOX_USERNAME / COMMS_SANDBOX_API_KEY are set in the environment; skipped
/// (marked inconclusive, not failed) otherwise so the rest of the suite stays green
/// without requiring real credentials. The wrong-credentials case always runs, since
/// it never touches a real account.
/// </summary>
[TestClass]
public class LiveSandboxSmokeTests
{
    private static bool TryGetSandboxCredentials(out string username, out string apiKey)
    {
        username = Environment.GetEnvironmentVariable("COMMS_SANDBOX_USERNAME") ?? "";
        apiKey = Environment.GetEnvironmentVariable("COMMS_SANDBOX_API_KEY") ?? "";
        return !string.IsNullOrEmpty(username) && !string.IsNullOrEmpty(apiKey);
    }

    [TestMethod]
    public async Task Authenticate_WithWrongCredentials_AgainstSandbox_Fails()
    {
        CommsSdk.UseSandBox();
        var sdk = await CommsSdk.Authenticate("invalid-user", "invalid-key-00000000000000000000000000000000");

        Assert.IsFalse(sdk.IsAuthenticated, "Authentication with an invalid credential unexpectedly succeeded.");
    }

    [TestMethod]
    public async Task SendSms_SingleNumber_AgainstSandbox_Succeeds()
    {
        if (!TryGetSandboxCredentials(out var username, out var apiKey))
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

    [TestMethod]
    public async Task SendSms_MultipleNumbers_AgainstSandbox_Succeeds()
    {
        if (!TryGetSandboxCredentials(out var username, out var apiKey))
        {
            Assert.Inconclusive("COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY not set; skipping live sandbox test.");
            return;
        }

        CommsSdk.UseSandBox();
        var sdk = await CommsSdk.Authenticate(username, apiKey);
        Assert.IsTrue(sdk.IsAuthenticated, "Authentication against the sandbox failed.");

        var numbers = new List<string> { "256700000000", "256700000001", "256700000002" };
        var success = await sdk.SendSms(numbers, "Golden-path multi-number smoke test from C# SDK");

        Assert.IsTrue(success, "SendSms with multiple numbers against the sandbox did not report success.");
    }

    [TestMethod]
    public async Task SendSms_OverOneThousandNumbers_AgainstSandbox_IsRejectedCleanly()
    {
        if (!TryGetSandboxCredentials(out var username, out var apiKey))
        {
            Assert.Inconclusive("COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY not set; skipping live sandbox test.");
            return;
        }

        CommsSdk.UseSandBox();
        var sdk = await CommsSdk.Authenticate(username, apiKey);
        Assert.IsTrue(sdk.IsAuthenticated, "Authentication against the sandbox failed.");

        var numbers = Enumerable.Range(0, 1001).Select(i => $"256700{i:D6}").ToList();

        ApiResponse? response = null;
        try
        {
            response = await sdk.QuerySendSms(numbers, "Oversized recipient list test");
        }
        catch (Exception ex)
        {
            Assert.Fail($"SendSms with >1000 numbers threw instead of returning a clean rejection: {ex}");
        }

        Assert.IsTrue(
            response is null || response.Status != ApiResponseCode.OK,
            "The API unexpectedly accepted a request with more than 1000 numbers.");
    }

    [TestMethod]
    public async Task BalanceMethods_AgainstSandbox_ReturnValidBalance()
    {
        if (!TryGetSandboxCredentials(out var username, out var apiKey))
        {
            Assert.Inconclusive("COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY not set; skipping live sandbox test.");
            return;
        }

        CommsSdk.UseSandBox();
        var sdk = await CommsSdk.Authenticate(username, apiKey);
        Assert.IsTrue(sdk.IsAuthenticated, "Authentication against the sandbox failed.");

        var balanceResponse = await sdk.QueryBalance();
        Assert.IsNotNull(balanceResponse, "QueryBalance returned null against the sandbox.");

        var balance = await sdk.GetBalance();
        Assert.IsNotNull(balance, "GetBalance returned null against the sandbox.");
        Assert.IsTrue(balance >= 0, "GetBalance returned a negative balance.");
    }
}
