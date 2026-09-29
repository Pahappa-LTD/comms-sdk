package com.pahappa.systems.commssdk.v1.utils;

import com.pahappa.systems.commssdk.v1.CommsSDK;
import com.pahappa.systems.commssdk.v1.models.ApiRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class NetworkHelper {
    public static String post(ApiRequest apiRequest, String apiUrl) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            byte[] body = CommsSDK.OBJECT_MAPPER.writeValueAsBytes(apiRequest);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }

            int status = conn.getResponseCode();

            InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

            String responseBody = "";
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    responseBody = reader.lines().collect(Collectors.joining("\n"));
                }
            }

            if (status < 200 || status >= 300) {
                throw new IOException("HTTP " + status + ": " + responseBody);
            }

            return responseBody;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
