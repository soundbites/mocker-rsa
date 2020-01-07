package nl.jcraane.mocker.features.forwarding

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import java.io.File

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

data class RecordedEntry(
    val requestPath: String,
    val method: Method,
    val contentType: ContentType,
    val responseBody: String = ""
)

enum class Method(val methodName: String) {
    GET("get"),
    POST("post"),
    PUT("put"),
    DELETE("delete"),
    PATCH("patch");

    companion object {
        fun create(httpMethod: HttpMethod) =
            values().firstOrNull { it.methodName.equals(httpMethod.value, true) }
    }
}

interface Persister {
    fun persist(recorder: Recorder)
}

class WarningPersister : Persister {
    override fun persist(recorder: Recorder) {
        throw IllegalStateException("When recording is enabled, make sure a Persister is configured.")
    }
}

class KtFilePersister(
    private val ktFilePath: String,
    private val resourcePath: String
) : Persister {
    private val startFile = """
        import io.ktor.application.call
        import io.ktor.http.HttpStatusCode
        import io.ktor.response.respond
        import io.ktor.response.respondText
        import io.ktor.routing.*
        import nl.jcraane.mocker.respondContents
        
        fun Route.recorded() {
        """.trimIndent()
    private val endFile = "}\n"

    override fun persist(recorder: Recorder) {
//        todo check if exist and use unique id
        File(ktFilePath).delete()

        val fileContents = StringBuilder().apply {
            append(startFile)

            recorder.data.map {
                append("${it.method.methodName}(\"${it.requestPath}\") {\n")
                endMethod(it)
            }

            append(endFile)
        }.toString()

        File(ktFilePath).writeText(fileContents)
    }

    private fun StringBuilder.endMethod(it: RecordedEntry): java.lang.StringBuilder? {
        if (it.responseBody.isNotEmpty()) {
//            todo append filename based on contenttype.
            val resourceFileName = "${it.method.methodName}_${it.requestPath.replace("/", "_")}.json"
            File(resourcePath).mkdirs()
            File(resourcePath, resourceFileName).writeText(it.responseBody)
            val classPathResourcePath = "/responses/recorded/$resourceFileName"
//            todo use correct content type and status
            append("call.respondContents(\"${classPathResourcePath}\")\n")
        } else {
            append("call.respond(HttpStatusCode.Created)\n")
        }
        return append("}\n\n")
    }
}