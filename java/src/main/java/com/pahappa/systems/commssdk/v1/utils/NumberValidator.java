package com.pahappa.systems.commssdk.v1.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NumberValidator {
    private static final String regex = "^\\+?(0|\\d{3})\\d{9}$";
    private static final Logger log = LoggerFactory.getLogger(NumberValidator.class);

    /**
     * Validates a list of phone numbers.
     * <p>
     * Handles conversion of numbers starting with '0' to '256' and removes duplicates.
     * Removes leading '+' signs and trims whitespace.
     * </p>
     * @param numbers List of number inputs to validate.
     * @return A clean list of numbers, with duplicates removed and formatted correctly.
     */
    public static List<String> validateNumbers(List<String> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            log.warn("Number list cannot be null or empty");
            return new ArrayList<>();
        }

        Set<String> _cleansed = new HashSet<>();
        for (String number : numbers) {
            if (number == null || number.trim().isEmpty()) {
                log.warn("Number ({}) cannot be null or empty!", number);
                continue;
            }
            number = number.trim().replaceAll("-|\\s", "");
            if (number.matches(regex)) {
                if (number.startsWith("0")) {
                    number = "256" + number.substring(1);
                } else if (number.startsWith("+")) {
                    number = number.substring(1);
                }
                _cleansed.add(number);
            } else {
                log.error("Number ({}) is not valid!", number);
            }
        }
        return new ArrayList<>(_cleansed);
    }
}
