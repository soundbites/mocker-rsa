package nl.jcraane.mocker

import io.ktor.application.*
import io.ktor.features.CORS
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import persons

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
//    install(TokenReplaceFeature)

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
