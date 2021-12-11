package protocol

class YDVP(
    private val startingLine: YdvpStartingLine,
    private val headers: List<YdvpHeader>,
    private val body: String = ""
) {
//    private val name = "YDVP"
//    private val version = "1.1"

    init {
        if (headers.isEmpty())
            throw Exception("Headers cannot be empty")
    }

    fun createRequest(): String {
        var requestString = "${startingLine as YdvpStartingLineQuery}\n"
        headers.forEach { requestString += it }

        requestString += "\n$body"

        return requestString
    }

    fun createResponse(): String {
        var requestString = "${startingLine as YdvpStartingLineResponse}\n"
        headers.forEach { requestString += it }
        requestString += "\n$body"

        return requestString
    }
}