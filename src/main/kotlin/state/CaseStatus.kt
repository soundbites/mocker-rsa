package state

import io.ktor.http.*
import kotlinx.serialization.Serializable
import mocks.RequestBreakdownReport
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.max
import kotlin.random.Random

class CaseStatus(var intakeStates: IntakeStates, var currentState: State) {

    val dateFormat: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss+0200")
        .withLocale(Locale.US)
        .withZone(ZoneId.of("GMT"))!!

    fun nextStatus(): CaseStatusMessage? {

        val elapsed = ChronoUnit.SECONDS.between(intakeStates.creationTime, LocalDateTime.now())
        val aanrijTijd = totalDuration - elapsed

        val eta = intakeStates.creationTime.plusSeconds((totalDuration * 2).toLong()).format(dateFormat)

        return when (currentState) {
            State.Dispatch -> {
                val dispatchStatus = intakeStates.nextStatus()
                if (dispatchStatus != null && !intakeStates.skipDispatch) {
                    return CaseStatusMessage(messageDetails = dispatchStatus, messageType = "DispatchLocationMessage")
                } else {
                    return CaseStatusMessage(messageDetails = CaseReceived(messageDetails = CaseReceivedMessageDetails(caseNumber = intakeStates.caseNumber), metadata = Metadata(caseNumber = intakeStates.caseNumber)), messageType = "CaseReceivedMessage")
                }
            }
            State.GEACCEPTEERD -> {
                return CaseStatusMessage(messageDetails =
                    TowTruckDetails(
                        caseNumber = intakeStates.caseNumber,
                        eta = null,
                        location = null,
                        status = "GEACCEPTEERD"
                    ), messageType = "TowingCompanyLocationMessage")
            }
            State.ONDERWEG -> {
                return CaseStatusMessage(messageDetails =
                    TowTruckDetails(
                        caseNumber = intakeStates.caseNumber,
                        eta = eta,
                        location = null,
                        status = "ONDERWEG"
                    ), messageType = "TowingCompanyLocationMessage")
            }
            State.ONDERWEG_WITH_LOCATION -> {
                return CaseStatusMessage(messageDetails =
                    TowTruckDetails(
                        caseNumber = intakeStates.caseNumber,
                        eta = eta,
                        location = Location(),
                        status = "ONDERWEG"
                    ), messageType = "TowingCompanyLocationMessage")
            }
            State.LADEN -> {
                return CaseStatusMessage(messageDetails =
                    TowTruckDetails(
                        caseNumber = intakeStates.caseNumber,
                        eta = eta,
                        location = Location(latitude = 52.68057205, longitude = 4.82921348),
                        status = "LADEN"
                    ), messageType = "TowingCompanyLocationMessage")
            }
        }
    }

    fun evolve(): CaseStatus? {

        val newIntakeState = intakeStates.evolve()
        if (newIntakeState != null) {
            intakeStates = newIntakeState
        }

        val elapsed = ChronoUnit.SECONDS.between(intakeStates.creationTime, LocalDateTime.now()).toInt()


        if (intakeStates.skipDispatch) {
            val durationPart = totalDuration / 4
            return when (elapsed) {
                in 0..durationPart -> CaseStatus(intakeStates, State.Dispatch)
                in durationPart+1..durationPart*2 -> CaseStatus(intakeStates, State.GEACCEPTEERD)
                in durationPart*2+1..durationPart*3 -> CaseStatus(intakeStates, State.ONDERWEG)
                in durationPart*3+1..durationPart*4 -> CaseStatus(intakeStates, State.ONDERWEG_WITH_LOCATION)
                in durationPart*4+1..durationPart*5 -> CaseStatus(intakeStates, State.LADEN)
                else -> null
            }
        } else {
            val durationPart = totalDuration / 5
            return when (elapsed) {
                in 0..durationPart*5 -> CaseStatus(intakeStates, State.Dispatch)
                in durationPart*5+1..durationPart*6 -> CaseStatus(intakeStates, State.GEACCEPTEERD)
                in durationPart*6+1..durationPart*7 -> CaseStatus(intakeStates, State.ONDERWEG)
                in durationPart*7+1..durationPart*8 -> CaseStatus(intakeStates, State.ONDERWEG_WITH_LOCATION)
                in durationPart*8+1..durationPart*9 -> CaseStatus(intakeStates, State.LADEN)
                else -> null
            }
        }

    }

    enum class State {
        Dispatch, GEACCEPTEERD, ONDERWEG, ONDERWEG_WITH_LOCATION, LADEN
    }
}

interface MessageDetails {}

@Serializable
data class CaseStatusMessage(
    val created: String = "2022-05-12T07:50:28.387+00:00",
    val messageDetails: MessageDetails,
    val messageType: String
)

@Serializable
data class TowTruckDetails(
    val caseNumber: Long,
    val destination: Destination = Destination(),
    val eta: String?,
    val location: Location?,
    val serviceNumber: String = "20225001033",
    val status: String
): MessageDetails

@Serializable
data class Destination(
    val latitude: Double = 52.68057205,
    val longitude: Double = 4.82921348,
    val name: String = "Laadlocatie",
    val purpose: String = "LAADLOCATIE"
)

@Serializable
data class Location(
    val latitude: Double = 52.680749999999996,
    val longitude: Double = 4.8342,
    val name: String = "Onbekend"
)

@Serializable
data class CaseReceived(
    val created: String = "2022-10-19T08:46:39.556+00:00",
    val messageDetails: CaseReceivedMessageDetails,
    val messageType: String = "CaseReceivedMessage",
    val metadata: Metadata
): MessageDetails

@Serializable
data class CaseReceivedMessageDetails(
    val assistancePhoneNumber: String = "+31703145615",
    val breakdownLocationCountry: String = "NL",
    val breakdownLocationCountryName: String = "Nederland",
    val caseNumber: Long,
    val contractLabelId: String = "ANWB",
    val contractMarket: String = "coma",
    val registrationTime: String = "2022-10-19T10:45:21.736+02:00",
    val smsSender: String = "ANWB"
)

@Serializable
data class Metadata(
    val assistancePhoneNumber: String = "+31703145615",
    val caseNumber: Long,
    val contractMarket: String = "coma",
    val smsSender: String ="ANWB"
)