package protocol

class YdvpStartingLineQuery(val method: String, val uri: String, val version: String) {
    override fun toString(): String {
        return "$method $uri YDVP/$version\n"
    }
}

class YdvpStartingLineResponse(val version: String, val responseCode: String, val explanation: String) {
    override fun toString(): String {
        return "YDVP/$version $responseCode $explanation"
    }
}