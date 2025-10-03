import kotlinx.coroutines.*
import v1.CommsSDK
import kotlin.system.*
import kotlin.test.Test
import kotlin.test.assertTrue

class StressTest {
    @Test
    fun main() = runBlocking {
        val numbers = mutableListOf<String>()
        for (i in 1..1000)
            numbers.add("0755123${(i - 1).toString().padStart(3, '0')}")
        val sdk = CommsSDK.authenticate("agabu-idaniel", "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99")
        val requests = 1_000
        val initialBalance = sdk.queryBalance()

        val time = measureTimeMillis {
            coroutineScope {
                val jobs = (1..requests).map {
                    async(Dispatchers.IO) {
                        sdk.sendSMS(numbers, "Hello from Kotlin Stress test!", "StressTest")
                    }
                }
                jobs.awaitAll()
            }
        }
        val finalBalance = sdk.queryBalance()

        println("Finished $requests requests in ${time}ms")

        val expectedCost = numbers.size * requests * 10.0
        val usedCost = (initialBalance!!.balance!! - finalBalance!!.balance!!)
        println("Results:\n")
        println("\tNumbers: ${numbers.size}, Requests: $requests")
        println("\tInitial Balance: ${initialBalance.balance}, Final Balance: ${finalBalance.balance}")
        assertTrue { initialBalance.balance!! > finalBalance.balance!! }
        println("\tMessage Cost: $usedCost, Expected Cost: $expectedCost")
        assertTrue { usedCost == expectedCost }
    }
}
