package mocks

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify
import nl.jcraane.mocker.extensions.respondContents
import state.IntakeStates
import state.Status

val meldingen = mutableListOf<IntakeStates>()

@UseExperimental(ImplicitReflectionSerializer::class)
fun Route.PechMeldingen() {
    get("disinfo/v1/pechMeldingen/relnr/{relnr}") {

        val nextStatus = meldingen.firstOrNull() {
            it.report.relation.relationNumber == call.parameters.get("relnr")
        }?.nextStatus()

        if (nextStatus == null) {
            call.respondContents("/responses/roadside-assistance-status-not-found.json")
        } else {
            call.respondText(Json.stringify(Status.serializer()))
        }
    }
}