package examples.helloexample

import client.InfluxServiceClient
import java.net.ConnectException

fun main() {
    val client = InfluxServiceClient("localhost", 6666)

    try {
        client.connect()
    } catch (exc: ConnectException) {
        println("Server is dead")
        return
    }

    client.sendGreetingMessageAndGetResponse()
    client.close()
}