package nl.jcraane.mocker.features

import io.ktor.http.HttpMethod

enum class Method(val methodName: String) {
    GET("get"),
    POST("post"),
    PUT("put"),
    DELETE("delete"),
    PATCH("patch"),
    HEAD("head"),
    OPTIONS("options"),
    ALL("*");

    companion object {
        fun create(httpMethod: HttpMethod) =
            values().firstOrNull { it.methodName.equals(httpMethod.value, true) } ?: ALL
    }
}