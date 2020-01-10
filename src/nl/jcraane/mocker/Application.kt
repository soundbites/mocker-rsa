package nl.jcraane.mocker

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import nl.jcraane.mocker.features.DetailLoggingFeature
import nl.jcraane.mocker.features.forwarding.RequestForwardingAndRecordingFeature
import nl.jcraane.mocker.features.TokenReplaceFeature
import nl.jcraane.mocker.features.UserAgentHostIpReplacementStrategy
import nl.jcraane.mocker.features.testing.ChaosMockerFeature
import nl.jcraane.mocker.features.testing.ChaosMockerFeature.Configuration.ResponseTimeBehavior
import nl.jcraane.mocker.features.testing.ChaosMockerFeature.Configuration.RequestMatcher
import org.slf4j.event.Level
import persons

fun main() {
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
    defaultFeatures()
    userDefinedFeatures()

//    Use interceptors for global logic.
    intercept(ApplicationCallPipeline.Call) {
        //        delay(1500)
    }

    // Static routing defined here
    routing {
        static("/static") {
            resources("static")
        }
    }

    // Mocks defined here
    mock(basePath = "api/v1") {
        persons()
    }
}

private fun Application.userDefinedFeatures() {
    install(TokenReplaceFeature) {
        hostIpReplacementStrategy = UserAgentHostIpReplacementStrategy(
            mapOf(
                "Android" to "10.0.2.2",
                "ios" to "localhost"
            )
        )
    }
    install(DetailLoggingFeature) {
        //        logDetails = DetailLoggingFeature.Configuration.LogDetail.values().toList()
    }
    install(RequestForwardingAndRecordingFeature) {
        forwardingConfig = RequestForwardingAndRecordingFeature.Configuration.ForwardingConfig(true, "http://localhost:8081")
        /*recordingConfig = RequestForwardingAndRecordingFeature.Configuration.RecorderConfig(
            true,
            KtFilePersister(
                "<INSERT_PATH>/mocker/src/mocks/Recorded.kt",
                "<INSERT_PATH>/mocker/resources/responses/recorded/"
            )
        )*/
    }
    install(ChaosMockerFeature) {
        slowResponseTimes.add(RequestMatcher.get("/"), ResponseTimeBehavior.Fixed(delay = 1500))
        slowResponseTimes.add(RequestMatcher.post("/person"), ResponseTimeBehavior.Random(variableDelay = 500L..1500L))
    }
}

private fun Application.defaultFeatures() {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost()
    }
    install(StatusPages) {
        status(*HttpStatusCode.allStatusCodes.filter { it.value >= 400 }.toTypedArray()) {
            DetailLoggingFeature.log("FAILED REQUEST", DetailLoggingFeature.logger) {
                DetailLoggingFeature.logger.info("${call.request.httpMethod} ${call.request.path()} failed with status $it")
            }
        }
    }
    install(DoubleReceive)
    install(CallLogging) {
        level = Level.DEBUG
    }
    install(DefaultHeaders) {
        header("X-Engine", "Mocker (Ktor)") // will send this header with each response
    }
}

