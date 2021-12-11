package server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Runtime.getRuntime
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class InfluxServiceClientHandler(val clientSocket: Socket) {
    fun run() {
        val bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

        val gotMessage = bufferedReader.readLine()
        /* And here comes protocol */

        if (gotMessage == "Hello message from socket") {
            val output = PrintWriter(clientSocket.getOutputStream(), true)
            output.println("Henlo")
        }
    }
}

class InfluxServiceServer(socketPort: Int) {
    private val serverSocket = ServerSocket(socketPort)

    init {
        getRuntime().addShutdownHook(Thread { serverSocket.close() })
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