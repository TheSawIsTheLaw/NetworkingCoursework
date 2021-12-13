package server

import config.InfluxdbConfiguration
import controllers.DataController
import controllers.services.DataService
import data.CharRepositoryImpl
import domain.dtos.AcceptMeasurementsListDTO
import gson.GsonObject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import protocol.YDVP
import protocol.YdvpHeader
import protocol.YdvpStartingLineRequest
import protocol.YdvpStartingLineResponse
import protocol.parser.YdvpParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Runtime.getRuntime
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class InfluxServiceClientHandler(private val clientSocket: Socket) {
    private val ydvpVersion = "0.1"
    private val defaultHeader = YdvpHeader("Server", "127.0.0.1")

    private val controller by inject<DataController>(DataController::class.java)

    private fun prepareUri(uri: String): List<String> {
        val parsedUri = uri.split("/").toMutableList()

        if (parsedUri[0] != "")
            throw Exception("URI format error")

        parsedUri.removeAt(0)
        return parsedUri
    }

    private fun controllerPostMethod(uri: String, body: String): YDVP {
        val parsedUri = prepareUri(uri)

        return when (parsedUri.first()) {
            "data" -> {
                if (parsedUri.size < 2)
                    throw Exception("Not enough inline arguments")
                val response = controller.addData(
                    parsedUri[1],
                    GsonObject.gson.fromJson(body, AcceptMeasurementsListDTO::class.java)
                )

                YDVP(
                    YdvpStartingLineResponse(
                        ydvpVersion,
                        response.statusCodeValue.toString(),
                        response.statusCode.name
                    ),
                    listOf(defaultHeader),
                    GsonObject.gson.toJson(response.body)
                )
            }
            else -> throw Exception("Unsupported URI")
        }
    }

    private fun controllerGetMethod(uri: String, body: String): YDVP {
        val parsedUri = prepareUri(uri)

        return when (parsedUri.first()) {
            "data" -> {
                if (parsedUri.size < 2)
                    throw Exception("Not enough inline arguments")
                val response =
                    controller.getData(parsedUri[1], GsonObject.gson.fromJson(body, listOf<String>().javaClass))

                YDVP(
                    YdvpStartingLineResponse(
                        ydvpVersion,
                        response.statusCodeValue.toString(),
                        response.statusCode.name
                    ),
                    listOf(defaultHeader),
                    GsonObject.gson.toJson(response.body)
                )
            }
            else -> throw Exception("Unsupported URI")
        }
    }

    private fun controllerWayByMethod(ydvpRequest: YDVP): YDVP {
        ydvpRequest.startingLine as YdvpStartingLineRequest
        val method = ydvpRequest.startingLine.method
        val uri = ydvpRequest.startingLine.uri

        val body = ydvpRequest.body

        return when (method) {
            "GET" -> controllerGetMethod(uri, body)
            "POST" -> controllerPostMethod(uri, body)
            else -> throw Exception("Unsupported method")
        }
    }

    fun run() {
        println("Accepted client on ${clientSocket.localSocketAddress} from ${clientSocket.remoteSocketAddress}")

        val bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

        var gotRequest = bufferedReader.readLine() + "\n"
        while (bufferedReader.ready())
            gotRequest += bufferedReader.readLine() + "\n"

        println("Got lines are: \n$gotRequest")
        val ydvpRequest = YdvpParser().parseRequest(gotRequest)

        /* And here comes protocol */
        /* FUS ROH DAH */
        val clientOut = PrintWriter(clientSocket.getOutputStream(), true)
        clientOut.println(controllerWayByMethod(ydvpRequest).createStringResponse())
    }
}

class InfluxServiceServer(socketPort: Int) {
    init {
        startKoin {
            modules(module {
                single { InfluxdbConfiguration() }

                single { CharRepositoryImpl() }

                single { DataService() }

                single { DataController() }
            })
        }
    }

    private val serverSocket = ServerSocket(socketPort)

    fun run() {
        getRuntime().addShutdownHook(Thread {
//            Ok, so... JVM just closes it all for me. And I cannot even reach it. Nice.
//            if (serverSocket.isClosed)
//                serverSocket.close()

            println("Server on port ${serverSocket.localPort} stopped")
        })

        if (!serverSocket.isBound || serverSocket.isClosed) {
            throw SocketException("Server socket is already in use")
        }

        println("Server started on port ${serverSocket.localPort}")

        while (true) {
            InfluxServiceClientHandler(serverSocket.accept()).run()
        }
    }
}