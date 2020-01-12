package nl.jcraane.mocker.features.forwarding

import nl.jcraane.mocker.getQueryParamNamePart
import java.io.File

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

//    todo make sure same path with multiple params are unique (use getQueryParamNamePart for this)
    private fun StringBuilder.endMethod(entry: RecordedEntry): java.lang.StringBuilder? {
        if (entry.responseBody.isNotEmpty()) {
//            todo append filename based on contenttype.
            val resourceFileName = "${entry.method.methodName}${entry.requestPath.replace("/", "_")}${getQueryParamNamePart(
                entry.queryParameters
            )}.json"
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
}