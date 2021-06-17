package mocks

import io.ktor.application.*
import io.ktor.routing.*
import nl.jcraane.mocker.extensions.respondContents

fun Route.PechMeldingen() {
    get("disinfo/v1/pechMeldingen") {
        call.respondContents("/responses/tasks.json")
    }
}