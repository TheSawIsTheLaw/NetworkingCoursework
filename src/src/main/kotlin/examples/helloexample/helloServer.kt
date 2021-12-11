package examples.helloexample

import server.InfluxServiceServer
import java.lang.Runtime.getRuntime

fun main() {
    val server = InfluxServiceServer(6666)

    getRuntime().addShutdownHook(Thread { server.stop() })

    server.run()
}