import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import nl.jcraane.mocker.extensions.respondContents

fun Route.tasks() {
    get("tasks") {
        call.respondContents("/responses/tasks.json")
    }

    post("tasks") {
        call.respond(HttpStatusCode.Created)
    }

    put("tasks") {
        call.respond(HttpStatusCode.Created)
    }

    delete("tasks") {
        call.respond(HttpStatusCode.NoContent)
    }
}