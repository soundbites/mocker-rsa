package nl.jcraane.mocker.features.forwarding

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.receiveText
import io.ktor.request.uri
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

class RequestForwardingAndRecordingFeature(private val configuration: Configuration) {
    private val httpClient = HttpClient()
    private val recorder = Recorder(configuration.recordingConfig?.persister ?: WarningPersister())

    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {
        val forwarding = configuration.forwardingConfig
        if (forwarding != null && forwarding.enabled) {
            val subject = context.subject
            val call = context.call
            if (subject is HttpStatusCode && subject == HttpStatusCode.NotFound) {
                val originUrl = "${forwarding.origin}/${call.request.path()}"
                logger.info("Request to ${call.request.uri} not found, trying $originUrl ")
                makeRequestToOriginAndReturnResponse(originUrl, call, context)
            }
        }
    }

    private suspend fun makeRequestToOriginAndReturnResponse(
        originUrl: String,
        call: ApplicationCall,
        context: PipelineContext<Any, ApplicationCall>
    ) {
        try {
            val result = httpClient.call(originUrl) {
                buildRequest(call)
            }
            val response = result.response
            if (response.status.isSuccess()) {
                val responseBody = response.readText(Charset.forName("UTF-8"))

                val contentType = response.contentType() ?: ContentType.Any
                if (configuration.recordingConfig?.enabled == true) {
                    Method.create(call.request.httpMethod)?.also {
                        recorder.record(RecordedEntry(call.request.path(), it, contentType, responseBody))
                    }
                }

                context.proceedWith(TextContent(responseBody, contentType, response.status))
            } else {
                logger.info("Request to $originUrl failed")
            }
        } catch (e: Exception) {
            logger.info("Request to $originUrl failed")
        }
    }

    private suspend fun HttpRequestBuilder.buildRequest(call: ApplicationCall) {
        method = call.request.httpMethod
        call.request.queryParameters.forEach { key, values ->
            values.forEach { value -> parameter(key, value) }
        }
        val body = call.receiveText()
        if (body.isNotEmpty()) {
            this.body = body
        }
    }

    class Configuration() {
        var forwardingConfig: ForwardingConfig? = null
        var recordingConfig: RecorderConfig? = null

        class ForwardingConfig(
            val enabled: Boolean = false,
            val origin: String
        )

        class RecorderConfig(
            val enabled: Boolean = false,
            val persister: Persister
        )
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RequestForwardingAndRecordingFeature> {
        private val logger: Logger = LoggerFactory.getLogger("RequestForwardingAndRecordingFeature")

        override val key = AttributeKey<RequestForwardingAndRecordingFeature>("RequestRecording")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): RequestForwardingAndRecordingFeature {
            val requestRecording = RequestForwardingAndRecordingFeature(
                Configuration().apply(
                    configure
                )
            )
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Before) {
                requestRecording.intercept(this)
            }
            return requestRecording
        }
    }
}