package server

import config.InfluxdbConfiguration
import controllers.DataController
import controllers.services.DataService
import data.CharRepositoryImpl
import domain.charrepository.CharRepositoryInterface
import domain.dtos.AcceptMeasurementsDTO
import domain.dtos.AcceptMeasurementsListDTO
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
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
import kotlin.system.exitProcess

class InfluxServiceClientHandler(private val clientSocket: Socket) {
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
        val startingLine = ydvpRequest.startingLine as YdvpStartingLineRequest
        if (startingLine.method == "GET") {
            val output = PrintWriter(clientSocket.getOutputStream(), true)
            output.println(
                YDVP(
                    YdvpStartingLineResponse("0.1", "200", "OK"),
                    listOf(YdvpHeader("Server", "127.0.0.1"))
                ).createStringResponse()
            )
        }
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

    private val controller by inject<DataController>(DataController::class.java)

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