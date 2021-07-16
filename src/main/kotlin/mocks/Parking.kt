package mocks

import io.ktor.application.call
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import nl.jcraane.mocker.extensions.getQueryParamNamePart
import nl.jcraane.mocker.extensions.getQueryParamsAsSet
import nl.jcraane.mocker.extensions.respondContents

var returnWithTransactions = false
var triedCounter = 0

fun Route.Parking() {

    post("v2/parking/transactions") {
        returnWithTransactions = true
        call.respond(HttpStatusCode.GatewayTimeout)
    }

    get("/v2/parking/transpondercards") {
        if (returnWithTransactions && triedCounter == 2) {
            call.respondContents("/responses/recorded/parkingTranspondercardsWithTransaction.json", ContentType.Application.Json)
        } else if (returnWithTransactions && triedCounter < 2) {
            triedCounter += 1
        } else {
            call.respondContents("/responses/recorded/parkingTranspondercardsWithout.json", ContentType.Application.Json)
        }
    }

}