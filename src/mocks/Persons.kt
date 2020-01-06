import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import nl.jcraane.mocker.respondContents

fun Route.persons() {
    get("persons") {
        call.respondContents("/responses/persons.json")
    }

    post("persons") {
        call.respond(HttpStatusCode.OK)
    }
}