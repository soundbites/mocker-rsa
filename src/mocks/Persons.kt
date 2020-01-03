import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.delay
import nl.capaxit.respondContents

fun Route.persons() {
    get("persons") {
//        delay(1000)
        call.respondContents("/responses/persons.json")
    }
}