package nl.jcraane.mocker.features.forwarding

import io.ktor.http.ContentType
import nl.jcraane.mocker.features.Method
import org.junit.Test
import kotlin.test.assertEquals

class KtFilePersisterTest {
    @Test
    fun writeRecording() {
        val sourceFileWriter = StringWriterStrategy()
        val recorder = Recorder(KtFilePersister(sourceFileWriter, StringWriterStrategy()))
        recorder.record(RecordedEntry("api/v1/persons", Method.GET, ContentType.Application.Json))
        recorder.record(RecordedEntry("api/v1/tasks", Method.GET, ContentType.Application.Json))
        recorder.record(RecordedEntry("api/v1/persons", Method.POST, ContentType.Application.Json))

        val expected = """
            import io.ktor.application.call
            import io.ktor.http.HttpStatusCode
            import io.ktor.response.respond
            import io.ktor.response.respondText
            import io.ktor.routing.*
            import io.ktor.http.ContentType
            import nl.jcraane.mocker.extensions.getQueryParamNamePart
            import nl.jcraane.mocker.extensions.getQueryParamsAsSet
            import nl.jcraane.mocker.extensions.respondContents

            fun Route.recorded() {get("api/v1/persons") {
            call.respond(HttpStatusCode.Created)
            }

            get("api/v1/tasks") {
            call.respond(HttpStatusCode.Created)
            }

            post("api/v1/persons") {
            call.respond(HttpStatusCode.Created)
            }

            }
            
        """.trimIndent()

        assertEquals(expected, sourceFileWriter.writer.toString())
    }
}