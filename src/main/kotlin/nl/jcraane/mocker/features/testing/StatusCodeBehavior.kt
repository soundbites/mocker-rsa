package nl.jcraane.mocker.features.testing

import io.ktor.http.HttpStatusCode

sealed class StatusCodeBehavior(val statusCode: HttpStatusCode) {

}