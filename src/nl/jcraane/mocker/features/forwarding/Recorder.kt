package nl.jcraane.mocker.features.forwarding

class Recorder {
    private val data: MutableSet<RecordedEntry> = mutableSetOf()

    fun record(entry: RecordedEntry) {
        data += entry
        persist()
    }

    private fun persist() {
        println("START PERSIST DATA")
        println(data)
        println("END PERSIST DATA")
    }
}

data class RecordedEntry(
    val requestPath: String,
    val httpMethod: String,
    val responseBody: String) {
    var persisted = false
}