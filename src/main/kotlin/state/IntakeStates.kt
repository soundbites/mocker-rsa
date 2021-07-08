package state

import com.sun.org.apache.xpath.internal.operations.Bool
import io.ktor.http.*
import kotlinx.serialization.Serializable
import mocks.RequestBreakdownReport
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.max


data class IntakeStates(val report: RequestBreakdownReport, var currentState: State, val creationTime: LocalDateTime) {

    fun nextStatus(): Status? {

        val elapsed = ChronoUnit.SECONDS.between(creationTime, LocalDateTime.now())

        return when (currentState) {
            State.Intake -> null
            State.Toewijzen -> Status.create(report, true, creationTime = creationTime)
            State.Onderweg -> {
                val status = Status.create(report, creationTime = creationTime)
                return status.copy(
                    fase = "ONDERWEG",
                    status = "A",
                    incident = status.incident.copy(
                        status = "A"
                    )
                )
            }
            State.Nadert -> {
                val status = Status.create(report, creationTime = creationTime)
                return status.copy(
                    fase = "NADERT",
                    status = "A",
                    incident = status.incident.copy(
                        status = "A"
                    )
                )
            }
            State.Dichtbij -> {
                val status = Status.create(
                    report, waitingTime = 960000 - (elapsed * 1000), creationTime = creationTime
                )

                val offset = max((0.8 - (elapsed.toDouble() / 100.0)), 0.0)

                val hulpVerlenerLocatie = Locatie(
                    x = 148329,
                    y = 433946,
                    lat = report.location.geoCoordinate.latitude,
                    lng = (report.location.geoCoordinate.longitude - offset),
                    desc = report.location.locationDescription
                )

                println("Locatie is $hulpVerlenerLocatie")

                return status.copy(
                    fase = "DICHTBIJ",
                    status = "A",
                    incident = status.incident.copy(
                        status = "A"
                    ),
                    hulpverlener = Hulpverlener(
                        locatie = hulpVerlenerLocatie,
                        voertuig = "W"
                    )
                )
            }
            State.Uitvoeren -> {
                val status = Status.create(report, creationTime = creationTime)
                return status.copy(
                    fase = "UITVOEREN",
                    status = "U",
                    incident = status.incident.copy(
                        status = "U"
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
        }
    }

    fun evolve(): IntakeStates? {

        val elapsed = ChronoUnit.SECONDS.between(creationTime, LocalDateTime.now()).toInt()

        return when (elapsed) {
            in 0..20 -> IntakeStates(report, State.Toewijzen, creationTime)
            in 21..40 -> IntakeStates(report, State.Onderweg, creationTime)
            in 41..60 -> IntakeStates(report, State.Nadert, creationTime)
            in 61..80 -> IntakeStates(report, State.Dichtbij, creationTime)
            in 81..100 -> IntakeStates(report, State.Uitvoeren, creationTime)
            else -> null
        }
    }

    enum class State {
        Intake, Toewijzen, Onderweg, Nadert, Dichtbij, Uitvoeren
    }

}

@Serializable
data class Status(
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
        fun create(report: RequestBreakdownReport, isBusier: Boolean = false, waitingTime: Long = 1944000, creationTime: LocalDateTime): Status {

            val dateFormat: DateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss+0200")
                .withLocale(Locale.US)
                .withZone(ZoneId.of("GMT"))!!

            return Status(
                gsonFile = "faseOpen.gson",
                fase = "OPEN",
                status = "-",
                statusTijd = dateFormat.format(creationTime),
                wachtTijd = waitingTime,
                incident = Incident(
                    sessie = "77A0955F6FDFFBEC",
                    volgNr = 53694,
                    aanmeldTijd = dateFormat.format(creationTime),
                    status = "O",
                    locatie = Locatie(
                        x = 148329,
                        y = 433946,
                        lat = report.location.geoCoordinate.latitude,
                        lng = report.location.geoCoordinate.longitude,
                        desc = report.location.locationDescription
                    ),
                    locatieBijz = report.incidentDescription ?: ""
                ),
                incFlags = IncFlags(
                    drukkerDanVerwacht = isBusier,
                    planningGewijzigd = false
                )
            )
        }
    }
}

@Serializable
data class IncFlags(
    val drukkerDanVerwacht: Boolean,
    val planningGewijzigd: Boolean
)

@Serializable
data class Hulpverlener(
    val locatie: Locatie,
    val voertuig: String
)

@Serializable
data class Incident(
    val sessie: String,
    val volgNr: Long,
    val aanmeldTijd: String,
    val status: String,
    val locatie: Locatie,
    val locatieBijz: String
)

@Serializable
data class Locatie(
    val x: Long,
    val y: Long,
    val lat: Double,
    val lng: Double,
    val desc: String
)
