package com.pahappa.systems.commssdk.v1.utils;

public class Log {
    public static void println(String message) {
        System.out.println("[CommsSDK]: " + message);
    }

    public static void printf(String format, Object... args) {
        println(String.format(format, args));
    }
}
