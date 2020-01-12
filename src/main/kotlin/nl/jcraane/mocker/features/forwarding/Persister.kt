package nl.jcraane.mocker.features.forwarding

import nl.jcraane.mocker.getQueryParamNamePart
import java.io.File

interface Persister {
    fun persist(recorder: Recorder)
}

class WarningPersister : Persister {
    override fun persist(recorder: Recorder) {
        throw IllegalStateException("When recording is enabled, make sure a Persister is configured.")
    }
}

// todo move actual writing of data to writer interface for easy unit testing.
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
        import nl.jcraane.mocker.getQueryParamNamePart
        import nl.jcraane.mocker.getQueryParamsAsSet
        import nl.jcraane.mocker.respondContents
        
        fun Route.recorded() {
        """.trimIndent()
    private val endFile = "}\n"

    override fun persist(recorder: Recorder) {
        File(ktFilePath).delete()

        val contents = buildString {
            append(startFile)

            recorder.data.forEach {
                append("${it.method.methodName}(\"${it.requestPath}\") {\n")
                endMethod(it)
            }

            append(endFile)
        }

        File(ktFilePath).writeText(contents)
    }

    //    todo make sure same path with multiple params are unique (use getQueryParamNamePart for this)
    private fun StringBuilder.endMethod(entry: RecordedEntry): java.lang.StringBuilder? {
        if (entry.responseBody.isNotEmpty()) {
//            todo append filename based on contenttype.
            val queryParamNamePart = getQueryParamNamePart(entry.queryParameters)
            val resourceStartPath = "${entry.method.methodName}${entry.requestPath.replace("/", "_")}"
            val resourceExtension = ".json"
            val resourceFileName = "$resourceStartPath$queryParamNamePart$resourceExtension"
            File(resourcePath).mkdirs()
            File(resourcePath, resourceFileName).writeText(entry.responseBody)
            val classPathResourcePath = "/responses/recorded/$resourceFileName"
//            todo use correct content type and status
            append("val queryParamNamePart = getQueryParamNamePart(getQueryParamsAsSet(call.parameters))\n")
//            todo we still need to add /responses/recorded here (the resource path specified in Application.kt
            append("call.respondContents(\"${resourceStartPath}\${queryParamNamePart}${resourceExtension}\")\n")
        } else {
            append("call.respond(HttpStatusCode.Created)\n")
        }
        return append("}\n\n")
    }
}