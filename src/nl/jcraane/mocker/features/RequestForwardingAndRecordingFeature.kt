package nl.jcraane.mocker.features

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey

class RequestForwardingAndRecordingFeature(private val configuration: Configuration) {
    class Configuration() {
        var forwardingEnabled: Boolean = false
        var recordingEnabled: Boolean = false
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RequestForwardingAndRecordingFeature> {
        override val key = AttributeKey<RequestForwardingAndRecordingFeature>("RequestRecording")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): RequestForwardingAndRecordingFeature {
            val requestRecording = RequestForwardingAndRecordingFeature(
                RequestForwardingAndRecordingFeature.Configuration().apply(configure)
            )
//            todo find out which phase we must bind to
            /*pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                tokenReplace.intercept(this)
            }*/
            return requestRecording
        }
    }
}