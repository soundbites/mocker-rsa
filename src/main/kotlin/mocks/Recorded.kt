import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.http.ContentType
import nl.jcraane.mocker.extensions.getQueryParamNamePart
import nl.jcraane.mocker.extensions.getQueryParamsAsSet
import nl.jcraane.mocker.extensions.respondContents

fun Route.recorded() {get("/v2/parking/transpondercards") {
val queryParamNamePart = getQueryParamNamePart(getQueryParamsAsSet(call.parameters))
call.respondContents("/responses/recorded/get_v2_parking_transpondercards${queryParamNamePart}.json", ContentType.Application.Json)
}

post("/v2/parking/transactions") {
val queryParamNamePart = getQueryParamNamePart(getQueryParamsAsSet(call.parameters))
call.respondContents("/responses/recorded/post_v2_parking_transactions${queryParamNamePart}.json", ContentType.Application.Json)
}

get("/v2/parking/polling/transaction/127256040") {
val queryParamNamePart = getQueryParamNamePart(getQueryParamsAsSet(call.parameters))
call.respondContents("/responses/recorded/get_v2_parking_polling_transaction_127256040${queryParamNamePart}.json", ContentType.Application.Json)
}

}
