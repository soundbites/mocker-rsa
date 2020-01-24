package nl.jcraane.mocker.features.variablereplacement

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.TextContent
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import nl.jcraane.mocker.extensions.UTF_8
import nl.jcraane.mocker.extensions.getQueryParamsAsMap
import nl.jcraane.mocker.extensions.isSupportedTextContentType

class VariableReplaceFeature(private val configuration: Configuration) {
    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {
        val call = context.call
        val message = context.subject
        if (message is TextContent) {
            val original = message.text
            var replaced = replaceVariables(original, call, configuration.tokens)
            if (configuration.useQueryParamsForReplacement) {
                replaced = replaceVariables(replaced, call, getQueryParamsAsMap(call.request.queryParameters))
            }
            if (replaced != original) {
                context.proceedWith(ByteArrayContent(replaced.toByteArray(UTF_8), message.contentType, call.response.status()))
            }
        } else if (message is ByteArrayContent && message.contentType?.isSupportedTextContentType() == true) {
            val original = String(message.bytes(), UTF_8)
            var replaced = replaceVariables(original, call, configuration.tokens)
            if (configuration.useQueryParamsForReplacement) {
                replaced = replaceVariables(replaced, call, getQueryParamsAsMap(call.request.queryParameters))
            }
            if (replaced != original) {
                context.proceedWith(ByteArrayContent(replaced.toByteArray(UTF_8), message.contentType, call.response.status()))
            }
        }
    }

    private fun replaceVariables(replaced: String, call: ApplicationCall, variables: Map<String, String>): String {
        var result = replaced
        result = result.replace(HOST_IP, configuration.hostIpReplacementStrategy.getHostIp(call))
        variables.forEach { (key, value) ->
            result = result.replace(getKey(key), value)
        }
        return result
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
        /**
         * If true, uses the value of the query parameters who's name matches the variable in the response as replacement for the
         * variable. Please be aware that any tokens which have the same as the query parameter have precedence.
         */
        var useQueryParamsForReplacement = false
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, VariableReplaceFeature> {
        const val TOKEN_START = "{"
        const val TOKEN_END = "}"
        const val HOST_IP = "${TOKEN_START}HOST_IP$TOKEN_END"

        override val key = AttributeKey<VariableReplaceFeature>("TokenReplace")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): VariableReplaceFeature {
            val tokenReplace = VariableReplaceFeature(
                Configuration().apply(configure)
            )
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                tokenReplace.intercept(this)
            }
            return tokenReplace
        }
    }
}