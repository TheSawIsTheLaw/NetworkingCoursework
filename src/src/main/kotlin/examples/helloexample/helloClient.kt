package examples.helloexample

import client.InfluxServiceClient
import protocol.YDVP
import protocol.YdvpHeader
import protocol.YdvpStartingLineRequest
import protocol.YdvpStartingLineResponse
import java.net.ConnectException

fun main() {
    val client = InfluxServiceClient("localhost", 6666)

    try {
        client.connect()
    } catch (exc: ConnectException) {
        println("Server is dead")
        return
    }

    client.sendRequestAndGetResponse(
        YDVP(
            YdvpStartingLineRequest("GET", "/lll/lll", "0.1"),
            listOf(YdvpHeader("Host", "127.0.0.1")),
            "{\n    \"smth\": \"lol\"\n}\n"
        )
    )

    client.close()
}