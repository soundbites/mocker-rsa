package nl.jcraane.mocker

import nl.jcraane.mocker.features.forwarding.QueryParam
import org.junit.Assert.assertEquals
import org.junit.Test

class MockFunctionsTest {
    @Test
    fun getQueryParamsNamePart() {
        assertEquals("", getQueryParamNamePart(emptySet()))
        assertEquals("?name=value", getQueryParamNamePart(setOf(QueryParam("name" to "value"))))
        assertEquals("?name=value&details=true", getQueryParamNamePart(setOf(
            QueryParam("name" to "value"),
            QueryParam("details" to "true"))))
    }
}