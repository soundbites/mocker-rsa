package nl.jcraane.mocker.features.forwarding

import io.ktor.http.ContentType
import nl.jcraane.mocker.features.Method
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

//    todo make sure same path with multiple params are unique
    private fun StringBuilder.endMethod(entry: RecordedEntry): java.lang.StringBuilder? {
        if (entry.responseBody.isNotEmpty()) {
//            todo append filename based on contenttype.
            val resourceFileName = "${entry.method.methodName}${entry.requestPath.replace("/", "_")}${getQueryParamNamePart(entry.queryParameters)}.json"
            File(resourcePath).mkdirs()
            File(resourcePath, resourceFileName).writeText(entry.responseBody)
            val classPathResourcePath = "/responses/recorded/$resourceFileName"
//            todo use correct content type and status
            append("call.respondContents(\"${classPathResourcePath}\")\n")
        } else {
            append("call.respond(HttpStatusCode.Created)\n")
        }
        return append("}\n\n")
    }

    private fun getQueryParamNamePart(queryParameters: Set<QueryParam>): String {
        return buildString {
            queryParameters
                .map { "${it.name}_${it.value}" }
                .forEachIndexed { index, text ->
                    val prefix = if (index == 0) "?" else "&"
                    append(prefix)
                    append(text)
                }
        }
    }
}