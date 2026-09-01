using System.Text.Json;
using Comms;
using Comms.Models;

namespace CommsTests;

/// <summary>
/// Regression protection for the SendSms/Balance golden path, using a local mock
/// HTTP server instead of the real API. These must keep passing across any future
/// SDK change so customers can always authenticate and send a message.
/// </summary>
[TestClass]
public class CommsSdkGoldenPathTests
{
    private MockApiServer _server = null!;

    [TestInitialize]
    public void Setup()
    {
        _server = new MockApiServer();
        CommsSdk.UseCustomServer(_server.Url);
    }

    [TestCleanup]
    public void Teardown()
    {
        _server.Dispose();
    }

    [TestMethod]
    public async Task Authenticate_SendsExplicitLocalWalletType_OnBalanceCheck()
    {
        // Regression guard: the live API rejects an explicit "walletType": null on
        // Balance requests (though it accepts the field being omitted). Sending an
        // explicit WalletType.Local avoids that class of bug entirely.
        _server.EnqueueResponse("""{"Status":"OK","Message":"Success","Balance":100.0}""");

        var sdk = await CommsSdk.Authenticate("user", "key");

        Assert.IsTrue(sdk.IsAuthenticated);
        Assert.AreEqual(1, _server.RequestBodies.Count);
        var request = JsonSerializer.Deserialize<ApiRequest>(_server.RequestBodies[0]);
        Assert.AreEqual("Balance", request!.Method);
        Assert.AreEqual(WalletType.Local, request.WalletType);
    }

    [TestMethod]
    public async Task SendSms_BuildsCorrectRequest_WithDefaultsAndExplicitLocalWalletType()
    {
        _server.EnqueueResponse("""{"Status":"OK","Balance":100.0}"""); // auth
        var sdk = await CommsSdk.Authenticate("user", "key");
        _server.EnqueueResponse("""{"Status":"OK","MsgFollowUpUniqueCode":"ABC123","Cost":35}""");

        var success = await sdk.SendSms("256700000000", "Hello world");

        Assert.IsTrue(success);
        var request = JsonSerializer.Deserialize<ApiRequest>(_server.RequestBodies[1]);
        Assert.AreEqual("SendSms", request!.Method);
        Assert.AreEqual(WalletType.Local, request.WalletType);
        Assert.AreEqual(1, request.MessageData!.Count);
        Assert.AreEqual("256700000000", request.MessageData[0].Number);
        Assert.AreEqual(MessagePriority.High, request.MessageData[0].Priority);
    }

    [TestMethod]
    public async Task SendSms_MultipleNumbers_AllIncludedInRequest()
    {
        _server.EnqueueResponse("""{"Status":"OK","Balance":100.0}""");
        var sdk = await CommsSdk.Authenticate("user", "key");
        _server.EnqueueResponse("""{"Status":"OK","MsgFollowUpUniqueCode":"BULK123"}""");

        var numbers = new List<string> { "256700000000", "256700000001" };
        var success = await sdk.SendSms(numbers, "Bulk message", "CustomSenderID", MessagePriority.Low);

        Assert.IsTrue(success);
        var request = JsonSerializer.Deserialize<ApiRequest>(_server.RequestBodies[1]);
        Assert.AreEqual(2, request!.MessageData!.Count);
        Assert.IsTrue(request.MessageData.All(m => m.SenderId == "CustomSenderID"));
        Assert.IsTrue(request.MessageData.All(m => m.Priority == MessagePriority.Low));
    }

    [TestMethod]
    public async Task SendSms_StatusFailed_ReturnsFalse()
    {
        _server.EnqueueResponse("""{"Status":"OK","Balance":100.0}""");
        var sdk = await CommsSdk.Authenticate("user", "key");
        _server.EnqueueResponse("""{"Status":"Failed","Message":"Insufficient balance"}""");

        var success = await sdk.SendSms("256700000000", "Hello world");

        Assert.IsFalse(success);
    }

    [TestMethod]
    public async Task QueryBalance_NoArgument_DefaultsToLocalWalletType()
    {
        _server.EnqueueResponse("""{"Status":"OK","Balance":100.0}"""); // auth
        var sdk = await CommsSdk.Authenticate("user", "key");
        _server.EnqueueResponse("""{"Status":"OK","Balance":50.0}""");

        await sdk.QueryBalance();

        var request = JsonSerializer.Deserialize<ApiRequest>(_server.RequestBodies[1]);
        Assert.AreEqual(WalletType.Local, request!.WalletType);
    }

    [TestMethod]
    public async Task QueryBalance_ExplicitInternational_IsRespected()
    {
        _server.EnqueueResponse("""{"Status":"OK","Balance":100.0}"""); // auth
        var sdk = await CommsSdk.Authenticate("user", "key");
        _server.EnqueueResponse("""{"Status":"OK","Balance":50.0}""");

        await sdk.QueryBalance(WalletType.International);

        var request = JsonSerializer.Deserialize<ApiRequest>(_server.RequestBodies[1]);
        Assert.AreEqual(WalletType.International, request!.WalletType);
    }

    [TestMethod]
    public async Task GetBalance_ParsesBalanceFromResponse()
    {
        _server.EnqueueResponse("""{"Status":"OK","Balance":100.0}""");
        var sdk = await CommsSdk.Authenticate("user", "key");
        _server.EnqueueResponse("""{"Status":"OK","Balance":73.5}""");

        var balance = await sdk.GetBalance();

        Assert.AreEqual(73.5, balance);
    }
}
