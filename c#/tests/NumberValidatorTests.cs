using Comms.Utils;

namespace CommsTests;

[TestClass]
public class NumberValidatorTests
{
    [TestMethod]
    public void ValidateNumbers()
    {
        var numbersToValidate = new List<string>
        {
            "256712345678",
            "+256712345678",
            "0712345678",
            "235-787-900-123",
            "+257 700 567 234",
            "0745"
        };

        var validated = NumberValidator.ValidateNumbers(numbersToValidate);

        Assert.IsNotNull(validated);
        Assert.AreEqual(3, validated.Count);
        Assert.IsTrue(validated.Contains("256712345678"));
        Console.WriteLine(string.Join(", ", validated));
    }
}
