package nl.jcraane.mocker

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
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
import nl.jcraane.mocker.features.variablereplacement.VariableReplaceFeature
import nl.jcraane.mocker.features.variablereplacement.UserAgentHostIpReplacementStrategy
import nl.jcraane.mocker.features.forwarding.FileWriterStrategy
import nl.jcraane.mocker.features.forwarding.KtFilePersister
import nl.jcraane.mocker.features.forwarding.RequestForwardingAndRecordingFeature
import nl.jcraane.mocker.features.testing.ChaosMockerFeature
import nl.jcraane.mocker.features.testing.RequestConfig
import nl.jcraane.mocker.features.testing.StatusCodeBehavior
import org.slf4j.event.Level
import persons
import tasks

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

    // Static routing defined here
    routing {
        static("/static") {
            resources("static")
        }
    }

    // Mocks defined here
    mock(basePath = "api/v1") {
        persons()
        tasks()
    }

    mock {
//        recorded()
    }
}

private fun Application.userDefinedFeatures() {
    install(VariableReplaceFeature) {
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
        recordingConfig = RequestForwardingAndRecordingFeature.Configuration.RecorderConfig(
            true,
            KtFilePersister(
                sourceFileWriter = FileWriterStrategy(
                    rootFolder = "/Users/jamiecraane/develop/IntelliJ/mocker/src/main/kotlin/mocks",
                    defaultFileName = "Recorded.kt"
                ),
                resourceFileWriter = FileWriterStrategy(
                    rootFolder = "/Users/jamiecraane/develop/IntelliJ/mocker/src/main/resources/responses/recorded/",
                    overwriteExistingFiles = true
                )
            ),
            recordQueryParameters = true
        )
    }

    install(ChaosMockerFeature) {
//        slowResponseTimes.add(RequestConfig.get("/api/v1/**"), ResponseTimeBehavior.Fixed(constant = 250))
//        slowResponseTimes.add(RequestConfig.post("/api/v1/**"), ResponseTimeBehavior.Random(variable = 500L..1500L, constant = 1500L))
        errorStatusCodes.add(RequestConfig.delete("/api/v1/tasks"), StatusCodeBehavior.Fixed(HttpStatusCode.Forbidden))
        errorStatusCodes.add(RequestConfig.post("/api/v1/tasks"), StatusCodeBehavior.Random(listOf(
            HttpStatusCode.Forbidden,
            HttpStatusCode.Unauthorized,
            HttpStatusCode.NotImplemented
        )))
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
