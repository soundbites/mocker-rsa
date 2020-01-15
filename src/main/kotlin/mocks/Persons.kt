
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.post
import io.ktor.routing.put

fun Route.persons() {
    /*get("persons") {
        call.respondContents("/responses/persons.json")
    }*/

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