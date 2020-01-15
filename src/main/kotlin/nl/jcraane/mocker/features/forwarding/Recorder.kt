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
    val responseBody: ByteArray? = null,
    val queryParameters: Set<QueryParam> = emptySet()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecordedEntry

        if (requestPath != other.requestPath) return false
        if (method != other.method) return false
        if (contentType != other.contentType) return false
        if (queryParameters != other.queryParameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestPath.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + queryParameters.hashCode()
        return result
    }
}

data class QueryParam(val name: String, val value: String) {
    companion object {
        operator fun invoke(param: Pair<String, String>) = QueryParam(param.first, param.second)
    }
}
