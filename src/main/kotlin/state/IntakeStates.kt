package state

import kotlinx.serialization.Serializable
import mocks.RequestBreakdownReport


data class IntakeStates(val report: RequestBreakdownReport, var currentState: State) {

    fun nextStatus(): Status? {
        return when(currentState) {
            State.Intake -> null
            State.Toewijzen -> Status.create(report)
            State.Onderweg -> {
                val status = Status.create(report)
                return status.copy(
                    fase = "ONDERWEG",
                    status = "A",
                    incident = status.incident.copy(
                        status = "A"
                    )
                )
            }
            State.Nadert -> {
                val status = Status.create(report)
                return status.copy(
                    fase = "NADERT",
                    status = "A",
                    incident = status.incident.copy(
                        status = "A"
                    )
                )
            }
            State.Dichtbij -> {
                val status = Status.create(report)
                return status.copy(
                    fase = "DICHTBIJ",
                    status = "A",
                    incident = status.incident.copy(
                        status = "A"
                    ),
                    hulpverlener = Hulpverlener(
                        locatie = Locatie(
                            x = 148329,
                            y = 433946,
                            lat = report.location.geoCoordinate.latitude,
                            lng = report.location.geoCoordinate.longitude,
                            desc = report.location.locationDescription
                        ),
                        voertuig = "W"
                    )
                )
            }
            State.Uitvoeren -> {
                val status = Status.create(report)
                return status.copy(
                    fase = "UITVOEREN",
                    status = "U",
                    incident = status.incident.copy(
                        status = "U"
                    )
                )
            }
        }
    }

    fun evolve(): IntakeStates? {
        return when(currentState) {
            State.Intake -> IntakeStates(report, State.Toewijzen)
            State.Toewijzen -> IntakeStates(report, State.Onderweg)
            State.Onderweg -> IntakeStates(report, State.Nadert)
            State.Nadert -> IntakeStates(report, State.Dichtbij)
            State.Dichtbij -> IntakeStates(report, State.Uitvoeren)
            State.Uitvoeren -> null
        }
    }

    enum class State {
        Intake, Toewijzen, Onderweg, Nadert, Dichtbij, Uitvoeren
    }

}
@Serializable
data class Status (
    val gsonFile: String,
    var fase: String,
    var status: String,
    val statusTijd: String,
    val wachtTijd: Long,
    val incident: Incident,
    val incFlags: IncFlags,
    var hulpverlener: Hulpverlener? = null
) {
    companion object {
        fun create(report: RequestBreakdownReport): Status {
            return Status(
                gsonFile = "faseOpen.gson",
                fase = "OPEN",
                status = "-",
                statusTijd = "2017-08-02T16:16:24+0000",
                wachtTijd = 1944000,
                incident = Incident(
                    sessie = "77A0955F6FDFFBEC",
                    volgNr = 53694,
                    aanmeldTijd = "2017-08-02T15:58:00+0000",
                    status = "O",
                    locatie = Locatie(
                        x = 148329,
                        y = 433946,
                        lat = report.location.geoCoordinate.latitude,
                        lng = report.location.geoCoordinate.longitude,
                        desc = report.location.locationDescription
                    ),
                    locatieBijz = report.location.locationDescription
                ),
                incFlags = IncFlags(
                    drukkerDanVerwacht = false,
                    planningGewijzigd = false
                )
            )
        }
    }
}

@Serializable
data class IncFlags (
    val drukkerDanVerwacht: Boolean,
    val planningGewijzigd: Boolean
)

@Serializable
data class Hulpverlener (
    val locatie: Locatie,
    val voertuig: String
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
