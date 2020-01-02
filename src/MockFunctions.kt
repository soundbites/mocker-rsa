package nl.capaxit

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondBytes
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.routing.routing

suspend fun ApplicationCall.respondContents(
    classPathResource: String,
    contentType: ContentType = ContentType.Application.Json
) {
    respondText(javaClass.getResource(classPathResource).readText(), contentType)
}

suspend fun ApplicationCall.respondFile(classPathResource: String, contentType: ContentType) {
    val resource = javaClass.getResource(classPathResource)
    val bytes = resource.readBytes()
    respondBytes(bytes, contentType)
}

fun Application.mock(basePath: String = "", build: Route.() -> Unit): Routing {
    return routing {
        route(basePath) {
            apply(build)
        }
    }
}