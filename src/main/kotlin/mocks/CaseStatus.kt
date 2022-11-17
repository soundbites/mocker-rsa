package mocks

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import nl.jcraane.mocker.extensions.respondContents
import state.*

val caseStatus = mutableListOf<CaseStatus>()

@UseExperimental(ImplicitReflectionSerializer::class)
fun Route.CaseStatus() {
    get("v1/casestatus/{casenumber}") {
        handleCaseStatusCall()
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleCaseStatusCall() {
    val state = caseStatus.firstOrNull() {
        it.intakeStates.report.relation.relationNumber == call.request.queryParameters["relationnumber"]
    }

    if (state == null) {
        call.respond(HttpStatusCode.NotFound, "relationnumber not found")
        return
    }

    val next = state.nextStatus()
    val evolved = state.evolve()

    caseStatus.remove(state)
    if (evolved != null) {
        caseStatus.add(evolved)
    }

    if (next != null) {
        val module = SerializersModule {
            polymorphic(MessageDetails::class) {
                Status::class with Status.serializer()
                TowTruckDetails::class with TowTruckDetails.serializer()
                CaseReceived::class with CaseReceived.serializer()
            }
        }


        call.respondText(Json(context = module).stringify(CaseStatusMessage.serializer(), next), ContentType.Application.Json)
    } else {
        call.respondContents("/responses/roadside-assistance-status-not-found.json", ContentType.Application.Json)
    }
}