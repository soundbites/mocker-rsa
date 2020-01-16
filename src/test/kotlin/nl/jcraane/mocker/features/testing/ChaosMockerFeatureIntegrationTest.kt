package nl.jcraane.mocker.features.testing

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import nl.jcraane.mocker.extensions.mock
import nl.jcraane.mocker.extensions.respondContents
import org.junit.Test
import kotlin.test.assertEquals

private fun Application.mockModule() {
    install(ChaosMockerFeature) {
        errorStatusCodes.add(RequestConfig.get("/api/v1/**"), StatusCodeBehavior.Fixed(HttpStatusCode.InternalServerError))
    }

    mock {
        get("api/v1/mock") {
            call.respondText("""response""")
        }

        get("api/v1/mock/bytes") {
            call.respondContents("/response.txt")
        }
    }
}

class ChaosMockerFeatureIntegrationTest {
    @Test
    fun testBasicMatchIncomingRequests() {
        withTestApplication(Application::mockModule) {
            handleRequest(HttpMethod.Get, "api/v1/mock")
        }.response.let {
            assertEquals(HttpStatusCode.InternalServerError, it.status())
        }

        withTestApplication(Application::mockModule) {
            handleRequest(HttpMethod.Get, "api/v1/mock/bytes")
        }.response.let {
            assertEquals(HttpStatusCode.InternalServerError, it.status())
        }
    }
}