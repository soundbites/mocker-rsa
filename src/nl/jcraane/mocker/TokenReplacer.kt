package nl.jcraane.mocker

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.content.TextContent
import io.ktor.util.pipeline.PipelineContext

class TokenReplacer {
    suspend fun interceptor(context: PipelineContext<Any, ApplicationCall>) {
        val call = context.call
        val message = context.subject
        if (message is TextContent) {
            val replaced = message.text.replace("{HOST_IP}", "10.0.0.2")
            context.proceedWith(TextContent(replaced, message.contentType, call.response.status()))
        }
    }
}