package nl.jcraane.mocker.features.testing

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import nl.jcraane.mocker.features.Method

class ChaosMockerFeature(private val configuration: Configuration) {
    suspend fun intercept(context: PipelineContext<Any, ApplicationCall>) {
        val call = context.call
        configuration.slowResponseTimes.bestMatchPath(call.request.path(), Method.create(call.request.httpMethod))?.delay()
    }

    class Configuration {
        val slowResponseTimes = SlowResponseTimes()
        val errorStatusCodes = ErrorStatusCodes()

        class SlowResponseTimes {
            private val responses = mutableMapOf<RequestMatcher, ResponseTimeBehavior>()

            fun add(matcher: RequestMatcher, responseTimeBehavior: ResponseTimeBehavior) {
                responses[matcher] = responseTimeBehavior
            }

            //                todo find best match based on path and method. Most specific one wins.
            fun bestMatchPath(path: String, method: Method): ResponseTimeBehavior? {
                return responses[RequestMatcher(method, path)]
            }
        }

        class ErrorStatusCodes {
            private val statusCodes = mutableMapOf<RequestMatcher, StatusCodeBehavior>()

            fun add(matcher: RequestMatcher, statusCodeBehavior: StatusCodeBehavior) {
                statusCodes[matcher] = statusCodeBehavior
            }

            //                todo find best match based on path and method. Most specific one wins.
            fun bestMatchPath(path: String, method: Method): StatusCodeBehavior? {
                return statusCodes[RequestMatcher(method, path)]
            }
        }

        //        todo add dsl method to construct query.
        data class RequestMatcher(val method: Method = Method.ALL, val path: String) {
            companion object {
                fun all(path: String) = RequestMatcher(Method.ALL, path)
                fun get(path: String) = RequestMatcher(Method.GET, path)
                fun post(path: String) = RequestMatcher(Method.POST, path)
                fun put(path: String) = RequestMatcher(Method.PUT, path)
                fun delete(path: String) = RequestMatcher(Method.DELETE, path)
                fun patch(path: String) = RequestMatcher(Method.PATCH, path)
            }
        }

        companion object {
            val ALL_PATHS = "/"
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