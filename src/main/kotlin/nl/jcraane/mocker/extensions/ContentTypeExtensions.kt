package nl.jcraane.mocker.extensions

import io.ktor.http.ContentType

private val supportedTextContent = setOf(
    ContentType.Application.Json,
    ContentType.Application.JavaScript,
    ContentType.Application.Rss,
    ContentType.Application.Xml,
    ContentType.Application.Xml_Dtd,
    ContentType.Text.Any,
    ContentType.Text.Plain,
    ContentType.Text.CSS,
    ContentType.Text.CSS,
    ContentType.Text.Html,
    ContentType.Text.JavaScript,
    ContentType.Text.VCard,
    ContentType.Text.Xml,
    ContentType.Text.EventStream
)

/**
 * Returns true if this ContentType is of the supportedTextContent types.
 */
fun ContentType.isSupportedTextContentType() = supportedTextContent.contains(this)