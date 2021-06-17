package state

import kotlinx.serialization.Serializable
import mocks.RequestBreakdownReport


data class IntakeStates(val report: RequestBreakdownReport, var currentState: State) {

    fun nextStatus(): Status? {
        return when(currentState) {
            State.Intake -> null
            State.Toewijzen -> TODO()
            State.Onderweg -> TODO()
            State.Nadert -> TODO()
            State.Dichtbij -> TODO()
            State.Uitvoeren -> TODO()
        }
    }

    enum class State {
        Intake, Toewijzen, Onderweg, Nadert, Dichtbij, Uitvoeren
    }

}
@Serializable
data class Status (
    val gsonFile: String,
    val fase: String,
    val status: String,
    val statusTijd: String,
    val wachtTijd: Long,
    val incident: Incident,
    val incFlags: IncFlags
)

@Serializable
data class IncFlags (
    val drukkerDanVerwacht: Boolean,
    val planningGewijzigd: Boolean
)

@Serializable
data class Incident (
    val sessie: String,
    val volgNr: Long,
    val aanmeldTijd: String,
    val status: String,
    val locatie: Locatie,
    val locatieBijz: String
)

@Serializable
data class Locatie (
    val x: Long,
    val y: Long,
    val lat: Double,
    val lng: Double,
    val desc: String
)
