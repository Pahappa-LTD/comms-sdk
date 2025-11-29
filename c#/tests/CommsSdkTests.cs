using Comms;
using Comms.Models;
//using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace CommsTests {

    [TestClass]
    public class CommsSdkTests
    {

        [TestMethod]
        public async Task CheckFunctionality()
        {
            CommsSdk.UseSandBox();
            string UserName = "sandbox";
            string ApiKey = "sandbox35";
            var sdk = await CommsSdk.Authenticate(UserName, ApiKey);
            var balance1 = await sdk.GetBalance();
            Assert.IsNotNull(balance1);
            Console.WriteLine($"Balance1: SHS.{balance1}");

            await sdk.SendSms("234", "testing");

            var numbers = new List<string> { "256789123456", "+256789123457", "256789123458" };
            await sdk.SendSms(numbers, "Sample SMS Message", "CustomSenderID", MessagePriority.Highest);

            var balance2 = await sdk.GetBalance();
            Assert.IsNotNull(balance2);
            Console.WriteLine($"Balance2: SHS.{balance2}");

            Assert.IsTrue(balance1 > balance2);
        }
    }
}