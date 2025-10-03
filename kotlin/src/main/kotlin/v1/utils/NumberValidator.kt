package v1.utils

object NumberValidator {
    private const val regex = "^\\+?(0|\\d{3})\\d{9}$"

    /**
     * Validates a list of phone numbers.
     *
     *
     * Handles conversion of numbers starting with '0' to '256' and removes duplicates.
     * Removes leading '+' signs and trims whitespace.
     *
     * @param numbers List of number inputs to validate.
     * @return A clean list of numbers, with duplicates removed and formatted correctly.
     */
    fun validateNumbers(numbers: List<String>): List<String> {
        if (numbers.isEmpty()) {
            System.err.println("Number list cannot be empty")
            return ArrayList()
        }

        val cleansed: MutableSet<String> = HashSet()
        for (number in numbers) {
            var number = number
            if (number.trim { it <= ' ' }.isEmpty()) {
                System.out.printf("Number (%s) cannot be empty!\n", number)
                continue
            }
            number = number.trim { it <= ' ' }.replace("-|\\s".toRegex(), "")
            if (number.matches(regex.toRegex())) {
                if (number.startsWith("0")) {
                    number = "256" + number.substring(1)
                } else if (number.startsWith("+")) {
                    number = number.substring(1)
                }
                cleansed.add(number)
            } else {
                System.out.printf("Number (%s) is not valid!\n", number)
            }
        }
        return ArrayList(cleansed)
    }
}
