import io.ktor.application.*
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveText
import io.ktor.response.ApplicationSendPipeline
import io.ktor.response.respondText
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelinePhase

fun main(args: Array<String>) {
    //io.ktor.server.netty.main(args) // Manually using Netty's EngineMain
    embeddedServer(
        Netty,
        watchPaths = listOf("mocker"),
        port = 8080,
        module = Application::module
    ).apply {
        start(wait = true)
    }
}

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost()
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

//    todo add config replace values.
    install(TokenReplaceFeature())

//    Use interceptors for global logic.
    intercept(ApplicationCallPipeline.Call) {
//        delay(1500)
    }

    routing {
        static("/static") {
            resources("static")
        }
    }

    mock(basePath = "api/v1") {
        persons()
    }
}

class TokenReplaceFeature : ApplicationFeature<ApplicationCallPipeline, Compression.Configuration, TokenReplacer> {
    override val key = AttributeKey<TokenReplacer>("replacer")

    override fun install(pipeline: ApplicationCallPipeline, configure: Compression.Configuration.() -> Unit): TokenReplacer {
        val feature = TokenReplacer()
        pipeline.sendPipeline.intercept(ApplicationSendPipeline.ContentEncoding) {
            feature.interceptor(this)
        }
        return feature
    }
}

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