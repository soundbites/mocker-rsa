package nl.jcraane.mocker.features.variablereplacement

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import nl.jcraane.mocker.extensions.mock
import nl.jcraane.mocker.extensions.respondContents
import org.junit.Test
import kotlin.test.assertEquals

private fun Application.mockModule() {
    install(VariableReplaceFeature) {
        hostIpReplacementStrategy = UserAgentHostIpReplacementStrategy(
            mapOf(
                "Android" to "10.0.2.2",
                "ios" to "localhost"
            )
        )
    }

    mock {
        get("api/v1/mock") {
            call.respondText("host: {HOST_IP}")
        }

        get("api/v1/mock/bytes") {
            call.respondContents("/mockResponse.txt", ContentType.Text.Plain)
        }
    }
}

class VariableReplacementFeatureTest {
    @Test
    fun replaceHostUserAgentTextContent() {
        withTestApplication(Application::mockModule) {
            handleRequest(HttpMethod.Get, "/api/v1/mock") {
                addHeader("User-Agent", "ios")
            }.response.let {
                    assertEquals("host: localhost", it.content)
                }

            handleRequest(HttpMethod.Get, "/api/v1/mock") {
                addHeader("User-Agent", "android")
            }.response.let {
                assertEquals("host: 10.0.2.2", it.content)
            }
        }
    }

    @Test
    fun replaceHostUserAgentBinaryContent() {
        withTestApplication(Application::mockModule) {
            handleRequest(HttpMethod.Get, "/api/v1/mock/bytes") {
                addHeader("User-Agent", "ios")
            }.response.let {
                assertEquals("host: localhost", it.content)
            }

            handleRequest(HttpMethod.Get, "/api/v1/mock") {
                addHeader("User-Agent", "android")
            }.response.let {
                assertEquals("host: 10.0.2.2", it.content)
            }
        }
    }
}