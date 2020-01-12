package nl.jcraane.mocker.features.forwarding

import io.ktor.http.ContentType
import nl.jcraane.mocker.features.Method

class Recorder(private val persister: Persister) {
    val data: MutableSet<RecordedEntry> = mutableSetOf()

    fun record(entry: RecordedEntry) {
        data += entry
        persist()
    }

    private fun persist() {
        persister.persist(this)
    }
}

/**
 * @param queryParameters parameters for the request. Please note at the moment every name must be unique (this is different
 * from the http spec where multiple parameters with the same name may exist. Please file an issue if you want this support.).
 */
data class RecordedEntry(
    val requestPath: String,
    val method: Method,
    val contentType: ContentType,
    val responseBody: String = "",
    val queryParameters: Set<QueryParam> = emptySet()
)

data class QueryParam(val name: String, val value: String)
