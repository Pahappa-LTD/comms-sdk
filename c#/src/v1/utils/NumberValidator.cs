using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace Comms.Utils;

public static class NumberValidator
{
    private static readonly Regex Regex = new(@"^\+?(0|\d{3})\d{9}$");

    public static List<string> ValidateNumbers(List<string> numbers)
    {
        if (numbers == null || !numbers.Any())
        {
            System.Console.Error.WriteLine("Number list cannot be null or empty");
            return new List<string>();
        }

        var cleansed = new HashSet<string>();
        foreach (var number in numbers)
        {
            if (string.IsNullOrWhiteSpace(number))
            {
                System.Console.Error.WriteLine($"Number ({number}) cannot be null or empty!");
                continue;
            }

            var trimmedNumber = number.Trim().Replace("-", "").Replace(" ", "");
            if (Regex.IsMatch(trimmedNumber))
            {
                if (trimmedNumber.StartsWith("0"))
                {
                    trimmedNumber = "256" + trimmedNumber.Substring(1);
                }
                else if (trimmedNumber.StartsWith("+"))
                {
                    trimmedNumber = trimmedNumber.Substring(1);
                }
                cleansed.Add(trimmedNumber);
            }
            else
            {
                System.Console.Error.WriteLine($"Number ({number}) is not valid!");
            }
        }
        return cleansed.ToList();
    }
}
