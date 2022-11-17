package mocks

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify
import nl.jcraane.mocker.extensions.respondContents
import state.IntakeStates
import state.Status

val meldingen = mutableListOf<IntakeStates>()

@UseExperimental(ImplicitReflectionSerializer::class)
fun Route.PechMeldingen() {
    get("disinfo/v1/pechmeldingen/relnr/{relnr}") {
        handlePechmeldingenCall()
    }
    get("disinfo/v1/pechmeldingen/relnr/{relnr}") {
        handlePechmeldingenCall()
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handlePechmeldingenCall() {
    val state = meldingen.firstOrNull() {
        it.report.relation.relationNumber == call.parameters.get("relnr")
    }

    val next = state?.nextStatus()
    val evolved = state?.evolve()

    if (state != null) {
        meldingen.remove(state)
    }
    if (evolved != null) {
        meldingen.add(evolved)
    }

    if (next != null) {
        call.respondText(Json.stringify(Status.serializer(), next), ContentType.Application.Json)
    } else {
        call.respondContents("/responses/roadside-assistance-status-not-found.json", ContentType.Application.Json)
    }
}