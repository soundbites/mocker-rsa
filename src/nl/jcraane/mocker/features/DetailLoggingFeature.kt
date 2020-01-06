package nl.jcraane.mocker.features

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.Headers
import io.ktor.http.content.TextContent
import io.ktor.request.receiveText
import io.ktor.response.ApplicationSendPipeline
import io.ktor.response.ResponseHeaders
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DetailLoggingFeature(private val configuration: Configuration) {
    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {
        val call = context.call
        val body = call.receiveText()

        logger.info("REQUEST HEADERS START")
        logHeaders(call.request.headers)
        logger.info("REQUEST HEADERS END")

        logger.info("RESPONSE HEADERS START")
        logResponseHeaders(call.response.headers)
        logger.info("RESPONSE HEADERS END")

        if (body.isNotEmpty()) {
            logger.info("REQUEST BODY START")
            logger.info(body)
            logger.info("REQUEST BODY END")
        }

        val message = context.subject
        if (message is TextContent) {
            logger.info("RESPONSE BODY START")
            logger.info(message.text)
            logger.info("RESPONSE BODY END")
        }
    }

    private fun logHeaders(headers: Headers) {
        headers.forEach { name, values ->
            values.forEach {
                logger.info("$name=$it")
            }
        }
    }

    private fun logResponseHeaders(headers: ResponseHeaders) {
        headers.allValues().forEach { name, values ->
            values.forEach {
                logger.info("$name=$it")
            }
        }
    }

    class Configuration() {

    }


    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, DetailLoggingFeature> {
        private val logger: Logger = LoggerFactory.getLogger("DetailLoggingFeature")

        const val TOKEN_START = "{"
        const val TOKEN_END = "}"
        const val HOST_IP = "${TOKEN_START}HOST_IP$TOKEN_END"

        override val key = AttributeKey<DetailLoggingFeature>("TokenReplace")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): DetailLoggingFeature {
            val tokenReplace = DetailLoggingFeature(
                Configuration().apply(configure)
            )
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                tokenReplace.intercept(this)
            }
            return tokenReplace
        }
    }
}