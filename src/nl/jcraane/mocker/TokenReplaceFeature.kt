package nl.jcraane.mocker

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.http.content.TextContent
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

class TokenReplaceFeature(configuration: Configuration) {
    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {
        val call = context.call
        val message = context.subject
        if (message is TextContent) {
            val replaced = message.text.replace("{HOST_IP}", "10.0.0.2")
            context.proceedWith(TextContent(replaced, message.contentType, call.response.status()))
        }
    }

    class Configuration {
        var tokens: List<String> = emptyList()
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, TokenReplaceFeature.Configuration, TokenReplaceFeature> {
        override val key = AttributeKey<TokenReplaceFeature>("CORS")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: TokenReplaceFeature.Configuration.() -> Unit
        ): TokenReplaceFeature {
            val cors = TokenReplaceFeature(TokenReplaceFeature.Configuration().apply(configure))
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                cors.intercept(this)
            }
            return cors
        }
    }
}