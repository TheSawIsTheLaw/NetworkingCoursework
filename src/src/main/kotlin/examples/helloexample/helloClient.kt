package examples.helloexample

import client.InfluxServiceClient

fun main() {
    val client = InfluxServiceClient("localhost", 6666)

    client.connect()
    client.sendGreetingMessageAndGetResponse()
    client.close()
}