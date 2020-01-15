package nl.jcraane.mocker

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.toMap
import nl.jcraane.mocker.features.forwarding.QueryParam
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("MockFunctions")

suspend fun ApplicationCall.respondContents(
    classPathResource: String,
    contentType: ContentType? = null
) {
    val resource = javaClass.getResource(classPathResource)
    if (resource != null) {
        respondBytes(resource.readBytes(), contentType ?: determineContentTypeOnFileExtensions(classPathResource))
    } else {
        log.error("Classpath resource [$classPathResource] cannot be found. Make sure it exists in src/main/resource${classPathResource.prependIfMissing("/")} and starts with a '/' (forward slash) character.")
        respond(HttpStatusCode.InternalServerError)
    }
}

fun Application.mock(basePath: String = "", build: Route.() -> Unit): Routing {
    return routing {
        route(basePath) {
            apply(build)
        }
    }
}

private fun determineContentTypeOnFileExtensions(resource: String): ContentType {
    return if (resource.indexOf(".") != -1) {
        val extension = resource.substringAfterLast(".")
        when (extension) {
            "json" -> ContentType.Application.Json
            "pdf" -> ContentType.Application.Pdf
            "xml" -> ContentType.Application.Xml
            "html" -> ContentType.Text.Html
            "jpg" -> ContentType.Image.JPEG
            "jpeg" -> ContentType.Image.JPEG
            "gif" -> ContentType.Image.GIF
            "png" -> ContentType.Image.PNG
            else -> ContentType.Text.Plain
        }
    } else {
        ContentType.Text.Plain
    }
}

fun String.prependIfMissing(value: String): String {
    return if (!this.startsWith(value)) {
        "$value$this"
    } else {
        this
    }
}

fun getQueryParamNamePart(queryParameters: Set<QueryParam>): String {
    return buildString {
        queryParameters
            .map { "${it.name}=${it.value}" }
            .forEachIndexed { index, text ->
                val prefix = if (index == 0) "?" else "&"
                append(prefix)
                append(text)
            }
    }
}

fun getQueryParamsAsSet(parameters: Parameters) = parameters.toMap()
    .map { entry -> QueryParam(entry.key, entry.value.first()) }
    .toSet()