package com.pahappa.systems.commssdk.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A tiny in-process HTTP server that stands in for the real Comms API during tests.
 * <p>
 * Using a real (loopback) HTTP server instead of mocking {@code RestTemplate} means the
 * request bodies asserted on here are exactly what Jackson/Spring actually put on the wire,
 * so a regression like "walletType silently stops being serialized" would be caught here.
 */
final class MockCommsApiServer implements AutoCloseable {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpServer server;
    private final List<String> capturedBodies = new CopyOnWriteArrayList<>();

    volatile String balanceStatus = "OK";
    volatile double balanceValue = 100.0;
    volatile String sendSmsStatus = "OK";
    volatile String sendSmsMessage = "Success";

    MockCommsApiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    List<String> capturedBodies() {
        return Collections.unmodifiableList(capturedBodies);
    }

    String lastCapturedBody() {
        if (capturedBodies.isEmpty()) {
            throw new IllegalStateException("No requests captured yet");
        }
        return capturedBodies.get(capturedBodies.size() - 1);
    }

    private void handle(HttpExchange exchange) throws IOException {
        String body;
        try (InputStream is = exchange.getRequestBody()) {
            body = new String(readAll(is), StandardCharsets.UTF_8);
        }
        capturedBodies.add(body);

        JsonNode json = OBJECT_MAPPER.readTree(body);
        String method = json.has("method") ? json.get("method").asText() : "";

        String responseBody;
        if ("Balance".equals(method)) {
            responseBody = "OK".equals(balanceStatus)
                    ? "{\"Status\":\"OK\",\"Message\":\"Success\",\"Balance\":" + balanceValue + "}"
                    : "{\"Status\":\"Failed\",\"Message\":\"Invalid credentials\"}";
        } else if ("SendSms".equals(method)) {
            responseBody = "OK".equals(sendSmsStatus)
                    ? "{\"Status\":\"OK\",\"Message\":\"" + sendSmsMessage + "\",\"MsgFollowUpUniqueCode\":\"ABC123\",\"Cost\":0.5}"
                    : "{\"Status\":\"Failed\",\"Message\":\"" + sendSmsMessage + "\"}";
        } else {
            responseBody = "{\"Status\":\"Failed\",\"Message\":\"Unknown method\"}";
        }

        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static byte[] readAll(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int read;
        while ((read = is.read(data)) != -1) {
            buffer.write(data, 0, read);
        }
        return buffer.toByteArray();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
