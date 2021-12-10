package server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Runtime.getRuntime
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class InfluxServiceClientHandler(val clientSocket: Socket) {
    fun run() {
        val bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

        val gotMessage = bufferedReader.readLines()
        /* And here comes protocol */
    }
}

class InfluxServiceServer(socketPort: Int) {
    private val serverSocket = ServerSocket(socketPort)

    init {
        getRuntime().addShutdownHook(thread { serverSocket.close() })
    }

    fun run() {
        if (!serverSocket.isBound || serverSocket.isClosed) {
            throw Exception("Server socket fatal error")
        }

        while (true) {
            InfluxServiceClientHandler(serverSocket.accept()).run()
        }
    }

    fun stop() {
        if (!serverSocket.isClosed)
            serverSocket.close()
    }
}


fun main() {

}