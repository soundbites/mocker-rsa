package nl.jcraane.mocker.features.forwarding

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import java.io.File

class Recorder {
    val data: MutableSet<RecordedEntry> = mutableSetOf()
    private val persister = KtFilePersister("Recording.kt")

    fun record(entry: RecordedEntry) {
        data += entry
        persist()
    }

    private fun persist() {
        persister.persist(this)
    }
}

data class RecordedEntry(
    val requestPath: String,
    val method: Method,
    val contentType: ContentType,
    val responseBody: String = "")

enum class Method(val methodName: String) {
    GET("get"),
    POST("post"),
    PUT("put"),
    DELETE("delete"),
    PATCH("patch");

    companion object {
        fun create(httpMethod: HttpMethod) =
            values().filter { it.methodName.equals(httpMethod.value, true) }.firstOrNull()
    }
}

interface Persister {
    fun persist(recorder: Recorder)
}

class KtFilePersister(val fullPath: String) : Persister {
    private val startFile = """
        import io.ktor.application.call
        import io.ktor.http.HttpStatusCode
        import io.ktor.response.respond
        import io.ktor.response.respondText
        import io.ktor.routing.*
        
        fun Route.recorded() {
        """.trimIndent()
    private val endFile = "}\n"

    override fun persist(recorder: Recorder) {
        File(fullPath).delete()

        val fileContents = StringBuilder().apply {
            append(startFile)

            recorder.data.map {
                when (it.method) {
                    Method.GET -> {
                        append("get(\"${it.requestPath}\") {\n")
                        endMethod(it)
                    }
                    Method.POST -> {
                        append("post(\"${it.requestPath}\") {\n")
                        endMethod(it)
                    }
                    Method.PUT -> {
                        append("put(\"${it.requestPath}\") {\n")
                        endMethod(it)
                    }
                    Method.DELETE -> {
                        append("delete(\"${it.requestPath}\") {\n")
                        endMethod(it)
                    }
                    Method.PATCH -> {
                        append("patch(\"${it.requestPath}\") {\n")
                        endMethod(it)
                    }
                }
            }

            append(endFile)
        }.toString()

        File(fullPath).writeText(fileContents)
    }

    private fun StringBuilder.endMethod(it: RecordedEntry): java.lang.StringBuilder? {
        if (it.responseBody.isNotEmpty()) {
            append("call.respondText(\"\"\"${it.responseBody}\"\"\")\n")
        } else {
            append("call.respond(HttpStatusCode.Created)\n")
        }
        return append("}\n\n")
    }
}