package v1.utils

import v1.CommsSDK
import v1.models.ApiRequest
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

class NetworkHelper {
    companion object {
        @Throws(IOException::class)
        fun post(apiRequest: ApiRequest, apiUrl: String): String {
            var conn: HttpURLConnection? = null
            try {
                val url = URL(apiUrl)
                conn = url.openConnection() as HttpURLConnection
                conn.setRequestMethod("POST")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.setDoOutput(true)
                conn.setDoInput(true)

                val body: ByteArray = CommsSDK.OBJECT_MAPPER.writeValueAsBytes(apiRequest)

                conn.getOutputStream().use { os ->
                    os.write(body)
                }
                val status = conn.getResponseCode()

                val stream = if (status in 200..<300) conn.inputStream else conn.errorStream

                var responseBody = ""
                if (stream != null) {
                    BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                        responseBody = reader.lines().collect(Collectors.joining("\n"))
                    }
                }

                if (status !in 200..<300) {
                    throw IOException("HTTP $status: $responseBody")
                }

                return responseBody
            } finally {
                conn?.disconnect()
            }
        }
    }
}