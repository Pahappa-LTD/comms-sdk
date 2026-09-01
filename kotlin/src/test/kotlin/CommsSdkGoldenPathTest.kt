import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import v1.CommsSDK
import v1.models.WalletType
import java.net.InetSocketAddress
import java.util.Collections
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Mocked golden-path coverage for SendSms/Balance, so a regression in the outgoing
 * request shape (e.g. the walletType-null bug that broke live authentication earlier
 * this session) fails a fast offline test instead of only surfacing against the live API.
 *
 * Uses a real local HttpServer rather than Spring's MockRestServiceServer, because
 * Validator.isValidCredential() constructs its own RestTemplate() instance internally
 * (not CommsSDK's shared companion client), so an interceptor-based mock bound only to
 * the shared client can't see the authentication request. A real loopback server is
 * seen by both RestTemplate instances without needing to touch production code.
 */
class CommsSdkGoldenPathTest {
    private val mapper = ObjectMapper()
    private val requestBodies = Collections.synchronizedList(mutableListOf<String>())
    private val responseQueue = Collections.synchronizedList(mutableListOf<String>())
    private lateinit var server: HttpServer

    @BeforeTest
    fun setUp() {
        requestBodies.clear()
        responseQueue.clear()
        server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/") { exchange ->
            val body = exchange.requestBody.readBytes().toString(Charsets.UTF_8)
            requestBodies.add(body)
            val response = if (responseQueue.isNotEmpty()) responseQueue.removeAt(0)
                else """{"Status":"Failed","Message":"no scripted response queued"}"""
            val bytes = response.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        CommsSDK.API_URL = "http://localhost:${server.address.port}/"
    }

    @AfterTest
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun sendSms_alwaysSendsExplicitLocalWalletTypeAndDefaultHighPriority() {
        responseQueue.add("""{"Status":"OK","Message":"OK","Balance":100.0}""")
        responseQueue.add("""{"Status":"OK","Message":"Sent","MsgFollowUpUniqueCode":"ABC123"}""")

        val sdk = CommsSDK.authenticate("user", "key")
        assertTrue(sdk.isAuthenticated)
        assertTrue(sdk.sendSMS("0700000000", "Hello from test!", "MyApp"))

        assertEquals(2, requestBodies.size, "Expected one Balance (auth) request and one SendSms request")
        val authRequest = mapper.readTree(requestBodies[0])
        assertEquals("Balance", authRequest["method"].asText())
        assertEquals("Local", authRequest["walletType"].asText())

        val sendRequest = mapper.readTree(requestBodies[1])
        assertEquals("SendSms", sendRequest["method"].asText())
        assertEquals("Local", sendRequest["walletType"].asText())
        assertEquals("1", sendRequest["msgdata"][0]["priority"].asText())
        assertEquals("MyApp", sendRequest["msgdata"][0]["senderid"].asText())
    }

    @Test
    fun sendSms_returnsFalseOnFailedStatus_withoutThrowing() {
        responseQueue.add("""{"Status":"OK","Message":"OK","Balance":100.0}""")
        responseQueue.add("""{"Status":"Failed","Message":"Insufficient balance"}""")

        val sdk = CommsSDK.authenticate("user", "key")
        assertFalse(sdk.sendSMS("0700000000", "Hello from test!", "MyApp"))
    }

    @Test
    fun queryBalance_defaultsToLocal_andRespectsExplicitInternational() {
        responseQueue.add("""{"Status":"OK","Message":"OK","Balance":100.0}""")
        val sdk = CommsSDK.authenticate("user", "key")
        assertTrue(sdk.isAuthenticated)

        responseQueue.add("""{"Status":"OK","Message":"OK","Balance":50.0}""")
        val response = sdk.queryBalance(WalletType.INTERNATIONAL)
        assertEquals(50.0, response?.balance)

        assertEquals(2, requestBodies.size)
        val balanceRequest = mapper.readTree(requestBodies[1])
        assertEquals("Balance", balanceRequest["method"].asText())
        assertEquals("International", balanceRequest["walletType"].asText())
    }
}
