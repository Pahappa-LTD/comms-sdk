# CommsSDK

Example:
```c#
var sdk = await CommsSdk.Authenticate("user", "password");
var balance1Str = await sdk.GetBalance();
var balance1 = long.Parse(balance1Str);
Console.WriteLine($"Balance1: SHS.{balance1}");

await sdk.SendSms("256789123456", "Single Number");

var numbers = new List<string> { "256789123456", "+256789123457", "256789123458" };
await sdk.SendSms(numbers, "Sample SMS Message to many numbers", "CustomSenderID", MessagePriority.Highest);

var balance2Str = await sdk.GetBalance();
var balance2 = long.Parse(balance2Str);
Console.WriteLine($"Balance2: SHS.{balance2}");

Assert.IsTrue(balance1 > balance2);
```