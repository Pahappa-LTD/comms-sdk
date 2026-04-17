package com.pahappa.systems.commssdk.v1.utils;

import junit.framework.TestCase;

import static com.pahappa.systems.commssdk.v1.utils.Log.*;

public class LogTest extends TestCase {

    public void testPrintln() {
        println("Test print ln");
    }

    public void testPrintf() {
        printf("Test print %s", "f");
    }
}