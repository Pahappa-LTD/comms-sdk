package v1.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object NumberValidator {
    private const val REGEX = "^\\+?(0|\\d{3})\\d{9}$"
    val log: Logger = LoggerFactory.getLogger(NumberValidator::class.java)

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
            log.warn("Number list cannot be empty")
            return ArrayList()
        }

        val cleansed: MutableSet<String> = HashSet()
        for (number in numbers) {
            var number = number
            if (number.trim { it <= ' ' }.isEmpty()) {
                log.warn("Number ({}) cannot be empty!", number)
                continue
            }
            number = number.trim { it <= ' ' }.replace("-|\\s".toRegex(), "")
            if (number.matches(REGEX.toRegex())) {
                if (number.startsWith("0")) {
                    number = "256" + number.substring(1)
                } else if (number.startsWith("+")) {
                    number = number.substring(1)
                }
                cleansed.add(number)
            } else {
                log.error("Number ({}) is not valid!", number)
            }
        }
        return ArrayList(cleansed)
    }
}
