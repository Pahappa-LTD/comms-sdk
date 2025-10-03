package com.pahappa.systems.egosmssdk.v1.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class NumberValidatorTest {

    @Test
    public void validateNumbers() {
        List<String> numbersToValidate = Arrays.asList(
                "256712345678", "+256712345678", "0712345678", "235-787-900-123",
                "+257 700 567 234", "0745"
        );
        List<String> validated = NumberValidator.validateNumbers(numbersToValidate);
        assertNotNull(validated);
        assertEquals(3, validated.size());
        assertTrue(validated.contains("256712345678"));
        System.out.println(validated);
    }
}