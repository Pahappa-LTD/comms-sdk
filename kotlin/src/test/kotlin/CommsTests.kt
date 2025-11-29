import v1.CommsSDK
import kotlin.test.Test
import kotlin.test.assertTrue
import com.fasterxml.jackson.databind.ObjectMapper

class CommsTests {
    val mapper = ObjectMapper()
    @Test
    fun test1() {
        val username = "sandbox"
        val apikey = "sandbox35"
        val sdk = CommsSDK.authenticate(username, apikey)
        println("Authenticated: ${sdk.isAuthenticated}")
        val response1 = sdk.queryBalance()
        println(mapper.writeValueAsString(response1))
        println("Balance 1: ${response1!!.balance}")
        assertTrue { sdk.sendSMS("0751234567", "Hello from Kotlin SDK!", "MyApp") }
        val response2 = sdk.queryBalance()
        println("Balance 2: ${response2!!.balance}")
        assertTrue { response1.balance!! > response2.balance!! }
    }
}