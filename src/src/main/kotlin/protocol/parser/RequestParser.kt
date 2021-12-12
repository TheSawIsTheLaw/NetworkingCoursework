package protocol.parser

import protocol.YDVP
import protocol.YdvpHeader
import protocol.YdvpStartingLineRequest

val ALLOWED_METHODS = listOf("GET", "POST")
val ALLOWED_VERSIONS = listOf("0.1")

class RequestParser {
    private fun parseStartingLine(line: String): YdvpStartingLineRequest {
        val tempLine = line.split(" ")

        val method = tempLine[0]
        if (method !in ALLOWED_METHODS)
            throw Exception("YDVP method type not allowed")


        val uri = tempLine[1]
        if (uri.first() != '/')
            throw Exception("URI format error")

        val version = tempLine[2].split("/")[1]
        if (version !in ALLOWED_VERSIONS)
            throw Exception("YDVP version not supported")

        return YdvpStartingLineRequest(method, uri, version)
    }

    private fun parseHeaders(headers: List<String>): List<YdvpHeader> {
        val outHeaders = mutableListOf<YdvpHeader>()

        var currentPairInHeader: List<String>
        for (currentHeader in headers) {
            currentPairInHeader = currentHeader.split(":").map { it.trim() }
            outHeaders.add(YdvpHeader(currentPairInHeader[0], currentPairInHeader[1]))
        }

        return outHeaders
    }

    fun parse(request: String): YDVP {
        val requestLines = request.split("\n").toMutableList()

        val startingLine = parseStartingLine(requestLines.first())

        requestLines.removeAt(0)
        val headers = parseHeaders(requestLines.takeWhile { it.isNotEmpty() })

        val body = requestLines.takeLastWhile { it.isNotEmpty() }.reduce { acc, it -> acc + "\n" + it }

        return YDVP(startingLine, headers, body)
    }
}

// A small test
//fun main()
//{
//    val stringToParse = "GET /user/pulse YDVP/0.1\n" +
//            "Host: 127.0.0.1\n" +
//            "\n" +
//            "{\n" +
//            "   \"someval\": 30\n" +
//            "}"
//
//    val parsed = RequestParser().parse(stringToParse)
//    println(parsed.startingLine)
//    println("Headers:")
//    parsed.headers.forEach { println(it) }
//    println(parsed.body)
//}