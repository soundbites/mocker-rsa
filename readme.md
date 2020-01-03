#Mocker

Mocker is a Ktor based server application which uses some extension methods and auto-reload to provide fast, flexible and easy mocking
of services using Ktor.

This approach differs from traditional mocking applications since the mocks are purely defined in code so the full power of Kotlin and DSL's can be used.

## Basic usage

CORS headers are installed bu default as is the serving of static content from the static folder under resources.

To create a simple endpoint, the Application.mock extension method can be used. This method sets up a routing with an optional basebath. The last argument to the mock extensions method is a Route.() so a lambda can be used to configure the mocks. See the following example:

```
mock(basePath = "api/v1") {
   get("persons") {
      call.respondText("someText") 
   }
}
```

which configure the api/v1/persons endpoint to return plain/text someText.

Responses can also be loaded from the claspath. The following example returns the responses/persons.json file (which is in src/main/resources/responses/persons.json) using the respondContents extensions method. Please note the default context-type of respondContents is application/json.

```
    get("persons") {
        call.respondContents("/responses/persons.json")
    }
```

### Delay for simulating slow responses

Sometimes you want to simulate slow loading of resources to test loaders in an application. This can easily be done using the delay function as in the following example:

```
    get("persons") {
        delay(1500)
        call.respondContents("/responses/persons.json")
    }
```

If you want to apply a global delay to all resources, an interceptor can be used like so (defined directly in the Application.module() function):

```
intercept(ApplicationCallPipeline.Call) {
        delay(1500)
}
```

## Running with auto-reload

To run Mocker with auto-reload do the following:

1. Run enableCompileOnChange.sh which uses Gradle to compile the sources when a change occurs on those sources.
2. Run runMocker.sh This will run the application (which is configured using auto-reload so that when the sources are re-compiled, the changes are piked up by the class loader)

To stop the application and/or auto-compile issue ctrl-c in both terminal windows.