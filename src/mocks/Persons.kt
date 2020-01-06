import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.persons() {
    get("persons") {
        call.respondContents("/responses/persons.json")
    }
}