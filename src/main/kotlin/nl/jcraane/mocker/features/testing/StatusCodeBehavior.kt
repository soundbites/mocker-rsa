package nl.jcraane.mocker.features.testing

import io.ktor.http.HttpStatusCode

sealed class StatusCodeBehavior {
    abstract fun getStatusCode(): HttpStatusCode
    /**
     * Configures a fixed status code to return by ChaosMocker for a given path.
     */
    class Fixed(val fixedStatusCode: HttpStatusCode) : StatusCodeBehavior() {
        override fun getStatusCode() = fixedStatusCode
    }
}