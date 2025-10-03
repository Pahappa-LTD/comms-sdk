package com.pahappa.systems.commssdk.v1.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NumberValidator {
    private static final String regex = "^\\+?(0|\\d{3})\\d{9}$";

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
            System.err.println("Number list cannot be null or empty");
            return new ArrayList<>();
        }

        Set<String> _cleansed = new HashSet<>();
        for (String number : numbers) {
            if (number == null || number.trim().isEmpty()) {
                System.out.printf("Number (%s) cannot be null or empty!\n", number);
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
                System.out.printf("Number (%s) is not valid!\n", number);
            }
        }
        return new ArrayList<>(_cleansed);
    }
}
