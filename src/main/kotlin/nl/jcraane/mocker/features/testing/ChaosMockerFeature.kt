package nl.jcraane.mocker.features.testing

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.request.ApplicationRequest
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import nl.jcraane.mocker.features.Method
import nl.jcraane.mocker.prependIfMissing
import org.springframework.core.util.AntPathMatcher

class ChaosMockerFeature(private val configuration: Configuration) {
    private val pathMatcher = AntPathMatcher()

    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {
        val call = context.call
        findBestMatch(call.request.httpMethod, call.request.path(), configuration.slowResponseTimes.getResponseTimesConfig())?.delay()
        findBestMatch(call.request.httpMethod, call.request.path(), configuration.errorStatusCodes.getStatusCodesConfig())?.also {
//            todo fix
//            context.proceedWith()
        }
    }

    fun <T> findBestMatch(requestMethod: HttpMethod, requestPath: String, config: Map<RequestConfig, T>): T? {
        val incomingRequest = RequestConfig(Method.create(requestMethod), requestPath.prependIfMissing("/"))
        val matched = config.filter {
            val path = it.key.path.prependIfMissing("/")
            val pathMatch = pathMatcher.match(path, incomingRequest.path)
            pathMatch && (it.key.method == incomingRequest.method || it.key.method == Method.ALL)
        }
        return matched.toList().maxBy { it.first.path.length }?.second
    }

    class Configuration {
        val slowResponseTimes = SlowResponseTimes()
        val errorStatusCodes = ErrorStatusCodes()

        class SlowResponseTimes {
            private val responses = mutableMapOf<RequestConfig, ResponseTimeBehavior>()

            fun add(config: RequestConfig, responseTimeBehavior: ResponseTimeBehavior) {
                responses[config] = responseTimeBehavior
            }

            fun getResponseTimesConfig() = responses.toMap()
        }

        class ErrorStatusCodes {
            private val statusCodes = mutableMapOf<RequestConfig, StatusCodeBehavior>()

            fun add(config: RequestConfig, statusCodeBehavior: StatusCodeBehavior) {
                statusCodes[config] = statusCodeBehavior
            }

            fun getStatusCodesConfig() = statusCodes.toMap()
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, ChaosMockerFeature> {

        override val key = AttributeKey<ChaosMockerFeature>("ChaosMocker")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): ChaosMockerFeature {
            val chaosMocker = ChaosMockerFeature(
                Configuration().apply(configure)
            )
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.After) {
                chaosMocker.intercept(this)
            }
            return chaosMocker
        }
    }
}