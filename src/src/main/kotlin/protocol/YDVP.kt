package protocol

class YDVP(
    val startingLine: YdvpStartingLine,
    val headers: List<YdvpHeader>,
    val body: String = ""
) {
//    private val name = "YDVP"
//    private val version = "1.1"

    init {
        if (headers.isEmpty())
            throw Exception("Headers cannot be empty")
    }

    fun createStringRequest(): String {
        var requestString = "${startingLine as YdvpStartingLineRequest}\n"
        headers.forEach { requestString += it }

        requestString += "\n$body"

        return requestString
    }

    fun createStringResponse(): String {
        var requestString = "${startingLine as YdvpStartingLineResponse}\n"
        headers.forEach { requestString += it }
        requestString += "\n$body"

        return requestString
    }
}