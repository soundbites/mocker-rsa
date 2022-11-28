package mocks

import kotlinx.serialization.Serializable
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.json.Json
import nl.jcraane.mocker.extensions.respondContents
import state.CaseStatus
import state.IntakeStates
import state.Status
import java.time.LocalDateTime

fun Route.RoadsideAssistance() {
    post("v2/roadside-assistance") {
        val report = Json.parse(RequestBreakdownReport.serializer(), call.receiveText())
        val intakeStates = IntakeStates(report, IntakeStates.State.Intake, LocalDateTime.now())
        meldingen.add(intakeStates)
        caseStatus.add(CaseStatus(intakeStates, CaseStatus.State.Dispatch))
        val response = RoadsideAssistanceResponseHolder(mocks.RoadsideAssistanceResponseHolder.Item(CaseNumber = intakeStates.caseNumber))
        call.respondText(Json.stringify(RoadsideAssistanceResponseHolder.serializer(), response), ContentType.Application.Json)
    }
}

@Serializable
data class RoadsideAssistanceResponseHolder(val ProcesInformatieItem: Item) {
    @Serializable
    data class Item(
        val Naam: String = "DIGITALE_INTAKE",
        val Waarde: String = "GESLAAGD",
        val CaseNumber: Long
    )
}

@Serializable
data class RequestBreakdownReport(
    val sourceSystem: String,
    val helpRequestType: RequestType,
    val incidentType: IncidentType,
    val transportation: TransportationInfo,
    val relation: UserInfo,
    val location: VehicleLocation,
    val incidentDescription: String? = null
) {

    @Serializable
    data class RequestType(
        val assistanceType: String,
        val incidentType: String? = null,
        val timestampOccurence: String
    )

    @Serializable
    data class IncidentType(
        val codeSystem: String,
        val symptomCode: Int? = null,
        val symptomName: String? = null
    )

    @Serializable
    data class TransportationInfo(
        val countryOfRegistration: String,
        val transportationType: String,
        val licensePlate: String
    )

    @Serializable
    data class UserInfo(
        val memberOfANWB: Boolean,
        val relationNumber: String,
        val phoneNumber: String,
        val phoneType: String
    )

    @Serializable
    data class VehicleLocation(
        val locationDescription: String,
        val locationType: String,
        val geoCoordinate: GeoCoordinate
    )

    @Serializable
    data class GeoCoordinate(
        val geoSyntax: String,
        val geoFormat: String,
        val accuracyType: String,
        val inaccuracy: Float,
        val latitude: Double,
        val longitude: Double
    )

}