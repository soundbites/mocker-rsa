package nl.jcraane.mocker.features.forwarding

import nl.jcraane.mocker.getQueryParamNamePart
import java.nio.file.Paths

interface Persister {
    fun persist(recorder: Recorder)
}

abstract class BasePersister: Persister {
    private val writtenEndpoints = mutableSetOf<String>()

    fun clear() {
        writtenEndpoints.clear()
    }

    fun savePath(path: String) {
        writtenEndpoints.add(path)
    }

    fun isWritten(path: String): Boolean {
        return writtenEndpoints.contains(path)
    }
}

class WarningPersister : BasePersister() {
    override fun persist(recorder: Recorder) {
        throw IllegalStateException("When recording is enabled, make sure a Persister is configured.")
    }
}

class KtFilePersister(
    private val sourceFileWriter: WriterStrategy,
    private val resourceFileWriter: WriterStrategy
) : BasePersister() {
    private val startFile = """
        import io.ktor.application.call
        import io.ktor.http.HttpStatusCode
        import io.ktor.response.respond
        import io.ktor.response.respondText
        import io.ktor.routing.*
        import io.ktor.http.ContentType
        import nl.jcraane.mocker.getQueryParamNamePart
        import nl.jcraane.mocker.getQueryParamsAsSet
        import nl.jcraane.mocker.respondContents
        
        fun Route.recorded() {
        """.trimIndent()
    private val endFile = "}\n"

    override fun persist(recorder: Recorder) {
        clear()
        val contents = buildString {
            append(startFile)
            recorder.data.forEach { recordedEntry ->
                writeResourceMethod(recordedEntry)
            }
            append(endFile)
        }

        sourceFileWriter.write(contents)
    }

    private fun StringBuilder.writeResourceMethod(entry: RecordedEntry) {
        val resourceStartPath = "${entry.method.methodName}${entry.requestPath.replace("/", "_")}"
        val resourceExtension = ".json"
        val path = "${entry.method.methodName}${entry.requestPath}"
        if (!isWritten(path)) {
            append("${entry.method.methodName}(\"${entry.requestPath}\") {\n")
            endMethod(entry, resourceStartPath, resourceExtension)
        }
        writeResourceFile(entry, resourceStartPath, resourceExtension)
        savePath(path)
    }

    private fun StringBuilder.endMethod(entry: RecordedEntry, resourceStartPath: String, resourceExtension: String): java.lang.StringBuilder? {
        if (entry.responseBody.isNotEmpty()) {
            val contentType = determineContentTypeOnFileExtensions(resourceExtension)
            append("val queryParamNamePart = getQueryParamNamePart(getQueryParamsAsSet(call.parameters))\n")
            val packageName = getResourcePackageName(resourceFileWriter.rootFolder)
            append("call.respondContents(\"$packageName${resourceStartPath}\${queryParamNamePart}${resourceExtension}\", $contentType)\n")
        } else {
            append("call.respond(HttpStatusCode.Created)\n")
        }
        return append("}\n\n")
    }

    private fun writeResourceFile(entry: RecordedEntry, resourceStartPath: String, resourceExtension: String) {
        val queryParamNamePart = getQueryParamNamePart(entry.queryParameters)
        val resourceFileName = "$resourceStartPath$queryParamNamePart$resourceExtension"
        resourceFileWriter.write(entry.responseBody, resourceFileName)
    }

    private fun getResourcePackageName(rootFolder: String): String {
        val resourcePath = Paths.get("src", "main", "resources").toAbsolutePath().toString()
        return rootFolder.substringAfterLast(resourcePath)
    }

    private fun determineContentTypeOnFileExtensions(resource: String): String {
        return if (resource.indexOf(".") != -1) {
            val extension = resource.substringAfterLast(".")
            when (extension) {
                "json" -> "ContentType.Application.Json"
                "pdf" -> "ContentType.Application.Pdf"
                "xml" -> "ContentType.Application.Xml"
                "html" -> "ContentType.Text.Html"
                "jpg" -> "ContentType.Image.JPEG"
                "jpeg" -> "ContentType.Image.JPEG"
                "gif" -> "ContentType.Image.GIF"
                "png" -> "ContentType.Image.PNG"
                else -> "ContentType.Text.Plain"
            }
        } else {
            "ContentType.Text.Plain"
        }
    }
}