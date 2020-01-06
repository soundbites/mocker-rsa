package nl.jcraane.mocker.features

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.content.TextContent
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

class TokenReplaceFeature(private val configuration: Configuration) {
    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {
        val call = context.call
        val message = context.subject
        if (message is TextContent) {
            var replaced = message.text
            replaced = replaced.replace(HOST_IP, configuration.hostIpReplacementStrategy.getHostIp(call))
            configuration.tokens.forEach { (key, value) ->
                replaced = replaced.replace(getKey(key), value)
            }
            if (replaced != message.text) {
                context.proceedWith(TextContent(replaced, message.contentType, call.response.status()))
            }
        }
    }

    private fun getKey(key: String): String {
        var keyWithTokens = key
        if (!key.startsWith(TOKEN_START)) {
            keyWithTokens = "$TOKEN_START$keyWithTokens"
        }
        if (!key.endsWith(TOKEN_END)) {
            keyWithTokens = "$keyWithTokens$TOKEN_END"
        }
        return keyWithTokens
    }

    class Configuration {
        var tokens: Map<String, String> = emptyMap()
        var hostIpReplacementStrategy: HostIpReplaceStrategy = StaticHostIpReplacementStrategy("localhost")
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, TokenReplaceFeature> {
        const val TOKEN_START = "{"
        const val TOKEN_END = "}"
        const val HOST_IP = "${TOKEN_START}HOST_IP$TOKEN_END"

        override val key = AttributeKey<TokenReplaceFeature>("TokenReplace")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): TokenReplaceFeature {
            val tokenReplace = TokenReplaceFeature(
                Configuration().apply(configure)
            )
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                tokenReplace.intercept(this)
            }
            return tokenReplace
        }
    }
}