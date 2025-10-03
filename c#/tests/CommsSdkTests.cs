using EgoSms;
using EgoSms.Models;
//using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace EgoSmsTests {

    [TestClass]
    public class EgoSmsSdkTests
    {
        [TestMethod]
        public async Task CheckFunctionality()
        {
            EgoSmsSdk.UseSandBox();
            Assert.AreEqual(EgoSmsSdk.ApiUrl, "http://sandbox.egosms.co/api/v1/json/");
            var sdk = await EgoSmsSdk.Authenticate("aganisandbox", "SandBox");
            var balance1Str = await sdk.GetBalance();
            Assert.IsNotNull(balance1Str);
            var balance1 = long.Parse(balance1Str);
            Console.WriteLine($"Balance1: SHS.{balance1}");

            await sdk.SendSms("234", "testing");

            var numbers = new List<string> { "256789123456", "+256789123457", "256789123458" };
            await sdk.SendSms(numbers, "Sample SMS Message", "CustomSenderID", MessagePriority.Highest);

            var balance2Str = await sdk.GetBalance();
            Assert.IsNotNull(balance2Str);
            var balance2 = long.Parse(balance2Str);
            Console.WriteLine($"Balance2: SHS.{balance2}");

            Assert.IsTrue(balance1 > balance2);
        }
    }
}

