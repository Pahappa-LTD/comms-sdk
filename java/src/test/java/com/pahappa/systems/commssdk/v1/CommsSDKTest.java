package com.pahappa.systems.commssdk.v1;

import org.junit.Test;
import com.pahappa.systems.commssdk.v1.models.MessagePriority;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CommsSDKTest {

    @Test
    public void checkFunctionality() {
//        CommsSDK.useSandBox();
        String userName = "sandbox";
        String apiKey = "sandbox35";
        CommsSDK sdk = CommsSDK.authenticate(userName, apiKey);
        double balance1 = sdk.getBalance();
        System.out.println("Balance1: SHS." + balance1);
        sdk.sendSMS("234", "testing");

        List<String> numbers = Arrays.asList("256789123456", "+256789123457", "256789123458");
        sdk.sendSMS(numbers, "Sample SMS Message", "CustomSenderID", MessagePriority.HIGHEST);
        double balance2 = sdk.getBalance();
        System.out.println("Balance2: SHS." + balance2);
        assertTrue(balance1 > balance2);
    }
}