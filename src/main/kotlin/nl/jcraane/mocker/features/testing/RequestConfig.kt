package nl.jcraane.mocker.features.testing

import nl.jcraane.mocker.features.Method

data class RequestConfig(val method: Method = Method.ALL, val path: String) {
    companion object {
        val ALL_PATHS = "/**"

        fun all(path: String) = RequestConfig(
            Method.ALL,
            path
        )

        fun get(path: String) = RequestConfig(
            Method.GET,
            path
        )

        fun post(path: String) = RequestConfig(
            Method.POST,
            path
        )

        fun put(path: String) = RequestConfig(
            Method.PUT,
            path
        )

        fun delete(path: String) = RequestConfig(
            Method.DELETE,
            path
        )

        fun patch(path: String) = RequestConfig(
            Method.PATCH,
            path
        )
    }
}