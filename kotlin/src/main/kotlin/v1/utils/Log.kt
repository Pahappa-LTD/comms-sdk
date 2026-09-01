package v1.utils

fun println(message: String?) {
    kotlin.io.println("[CommsSDK]: $message");
}

fun printf(message: String, vararg args: Any?) {
    System.out.printf("[CommsSDK]: $message", args);
}
