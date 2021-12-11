package client

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class InfluxServiceClient(private val ip: String, private val port: Int) {
    private var clientSocket: Socket? = null
    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    fun connect() {
        clientSocket = Socket(ip, port)
        output = PrintWriter(clientSocket!!.getOutputStream(), true)
        input = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
    }

    fun sendGreetingMessageAndGetResponse(): String? {
        output?.println("Hello message from socket")
        val response = input?.readLine()
        println("Got in sendGreet: $response")

        return response
    }

    fun close() {
        output?.close()
        input?.close()

        if (clientSocket == null)
            return

        if (!clientSocket!!.isClosed || clientSocket!!.isBound || clientSocket!!.isConnected)
            clientSocket!!.close()
    }
}