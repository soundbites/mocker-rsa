package nl.jcraane.mocker.features

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

class DetailLoggingFeature(private val configuration: Configuration) {
    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {

    }

    class Configuration() {

    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, DetailLoggingFeature.Configuration, DetailLoggingFeature> {
        const val TOKEN_START = "{"
        const val TOKEN_END = "}"
        const val HOST_IP = "${TOKEN_START}HOST_IP$TOKEN_END"

        override val key = AttributeKey<DetailLoggingFeature>("TokenReplace")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: DetailLoggingFeature.Configuration.() -> Unit
        ): DetailLoggingFeature {
            val tokenReplace = DetailLoggingFeature(
                DetailLoggingFeature.Configuration().apply(configure)
            )
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                tokenReplace.intercept(this)
            }
            return tokenReplace
        }
    }

}