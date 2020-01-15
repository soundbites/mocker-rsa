
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import nl.jcraane.mocker.respondContents

fun Route.persons() {
    get("persons") {
        call.respondContents("/responses/persons.json")
    }

    post("persons") {
        call.respond(HttpStatusCode.Created)
    }
    put("persons") {
        call.respond(HttpStatusCode.Created)
    }

    delete("persons") {
        call.respond(HttpStatusCode.NoContent)
    }
}