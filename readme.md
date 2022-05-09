# Mocker

Mocker is a Ktor based server application which uses some extension methods and auto-reload to provide fast, flexible and easy mocking
of services using Ktor.

This approach differs from traditional mocking applications since the mocks are purely defined in code so the full power of Kotlin and DSL's can be used.

## Running mocker in Docker

Run `docker build -t rsa_mocker .` to build the image
then run `docker run -p 8080:8080 rsa_mocker`

## Basic usage

CORS headers are installed by default as is the serving of static content from the static folder under resources.

To create a simple endpoint, the Application.mock extension method can be used. This method sets up a routing with an optional basebath. The last argument to the mock extensions method is a Route.() so a lambda can be used to configure the mocks. See the following example:

```
mock(basePath = "api/v1") {
   get("persons") {
      call.respondText("someText") 
   }
}
```

which configure the api/v1/persons endpoint to return plain/text someText.

Responses can also be loaded from the classpath. The following example returns the responses/persons.json file (which is in src/main/resources/responses/persons.json) using the respondContents extensions method. Please note the default context-type of respondContents is application/json.

```
    get("persons") {
        call.respondContents("/responses/persons.json")
    }
```

### Websockets

Mocker has support for mocking websocket connections. By default the following extensions functions van be used to create a web socket which sends a continuous stream of events or one which echos the incoming request. Those are defined as follows:

```
mockWebSocket("api/v2/websocket") {
    sendContinuousResponse("Hello from websocket", 1000L)
}

mockWebSocket("api/v2/echo") {
    echoRequest()
}
```

Because this feature uses the Ktor WebSockets feature, the full power of this feature is available.

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

## Metrics

Although metrcis might not sound that useful for a mocking tool, it can be really helpful to know what specific requests and how many requests an application makes. This can be visualized with the metrics plugin.

Metrics of the called services are available using the DropWizard feature which is installed by default. BNY default the JMXReporter is configured buu any DropWizard reporter can be used. The metrics feature is defined in the metrics function in Application.kt. To monitor Mocker with jvisualvm start jvisualvm from the command line. Also make sure the MBeans plugin is installed in jvisualvm. This can be done via: Tools -> Plugins -> Available Plugins and install the VisualVM-MBeans plugin.

See [https://ktor.io/servers/features/metrics.html] for more information about the metrics feature and [https://metrics.dropwizard.io/4.0.0/getting-started.html] for more information about DropWizard and the different reporters.

## Advanced usage

### replace variables in responses

Replacing variables in responses have different use cases. Consider the example of when a response contains an absolute URL to another resource (a PDF for example). 

```
{
    "name": "invoice",
    "link": "http://localhost/invoice.pdf",
    "greeting", "Hello something"
}
```

And you want the host to be localhost for iOS user-agents and 10.0.2.2 for Android emulator user-agents. For this, the TokenReplaceFeature feature can be used. This feature can replace arbitrary keys and some fixed keys with more advanced replacement strategies. The following example replaces the {NAME} key with something fixed and {HOST_IP} with a host identifier based on the user-agent:

```
{
    "name": "invoice",
    "link": "http://{HOST_IP}/invoice.pdf",
    "greeting", "Hello {NAME}"
}

install(TokenReplaceFeature) {
    tokens = mapOf(
        "NAME" to "John"
    )
    hostIpReplacementStrategy = UserAgentHostIpReplacementStrategy(mapOf(
        "Android" to "10.0.2.2",
        "ios" to "localhost"
    ))
}
```  

### Detailed logging

To log more than just the path, the DetailLoggingFeature can be used. See the LogDetail enum for the items that
can be logged. Since Logback is used, logging can easily be routed to a file if desired. The following example
configured logging the request headers and bodies:

```
install(DetailLoggingFeature) {
    logDetails = listOf(DetailLoggingFeature.Configuration.LogDetail.REQUEST_HEADERS, DetailLoggingFeature.Configuration.LogDetail.REQUEST_BODY)
}
```

### Chaos Mocker

You might want to test how an application behaves when not all backend services behave nicely, for example when services are performing slow or with inconsistent response times or when services throw errors. The ChaosMocker feature can be used to simulate slow responses and/or error responses. 

With the ChaosMocker feature an incoming request path is mapped onto a behavior. At the moment there are two kind of behaviors:

- ResponseTimeBehavior
    Fixed, adds a fixed delay to every request which matches
    Random, adds a random delay to every requests which matches
- StatusCodeBehavior
    Returns the configured status code for every path that matches
    
Matching is done on path and http method. This path can be configured like an Ant Style path [https://ant.apache.org/manual/dirtasks.html], for example

- /api/v1/** Matches all paths which begins with /api/v1
- /api/v1/persons Matches the exact path /api/v1/persons

The http method is matched based on the configured method or all methods if RequestConfig.all is used. See the following example:

```
install(ChaosMockerFeature) {
    slowResponseTimes.add(RequestConfig.get("/api/v1/**"), ResponseTimeBehavior.Fixed(constant = 250))
    slowResponseTimes.add(RequestConfig.post("/api/v1/**"), ResponseTimeBehavior.Random(variable = 500L..1500L, constant = 1500L))
    slowResponseTimes.add(RequestConfig.all("/api/v1/persons/actions"), ResponseTimeBehavior.Random(variable = 500L..1500L, constant = 1500L))
    errorStatusCodes.add(RequestConfig.delete("/api/v1/tasks"), StatusCodeBehavior(HttpStatusCode.Forbidden))
}
```

### Request Forwarding

Not all API's have to be mocked. Take for example the following endpoints for a given API:

- api/v1/persons
- api/v1/tasks

Suppose api/v1/persons is in place and api/v1/tasks is to be developed but the url's, requests and responses are already known. In this situation the api/v1/tasks can be mocked while all other requests can be forwarded to the real API. The following example shows how this is configured by using the RequestForwardingAndRecordingFeature feature:

```
install(RequestForwardingAndRecordingFeature) {
    forwardingConfig = RequestForwardingAndRecordingFeature.Configuration.ForwardingConfig(
        enabled = true,
        origin = "https://somerealapi:8081"
    )
}
```

### Request Recording

The requests and responses which are forwared to the origin can also be recorded and, after recorded, served by Mocker. This is handy when you want to record a specific scenario for testing (and to be able to alter the responses for different test scenarios). The following example show how to configure recording of requests and responses:

```
 install(RequestForwardingAndRecordingFeature) {
    forwardingConfig = RequestForwardingAndRecordingFeature.Configuration.ForwardingConfig(
        enabled = true,
        origin = "https://somerealapi:8081"
    )
    recordingConfig = RequestForwardingAndRecordingFeature.Configuration.RecorderConfig(
        enabled = true,
        persister = KtFilePersister(
            sourceFileWriter = FileWriterStrategy(
                rootFolder = "<FOLDER>/mocker/src/main/kotlin/mocks",
                defaultFileName = "Recorded.kt"
            ),
            resourceFileWriter = FileWriterStrategy(rootFolder = "<FOLDER>/mocker/src/main/resources/responses/recorded/")
        ),
        recordQueryParameters = true
    )
}
```

To record requests and responses, forwarding all unhandled requests to an origin must be enabled. A recorded request is unique based on its method and path, so GET /api/v1/persons is different from POST /api/v1/persons. By default query parameters do not make the recorded request unique so /api/v1/persons?name=John and /api/v1/persons?name=Cleese are treated as the same request and only one response is recorded. To make the query parameters unique for each recorded request (which also makes the response unique for the given parameters), set recordQueryParameters to true.

The persister is responsible for writing the recorded data to file. The KtFilePersister writes the requests to a Kotlin file (the sourceFileWriter) and the individual responses to separate files in the folder specified by the resourceFileWriter.

In the example above the source file with the endpoints (Recorded.Kt) and the resource files, are both written to the src/main/kotlin and src/main/resources respectively. 

The method which encapsulates the recorded endpoints is called recorded. See the following example of a Recorded.kt file:

```
fun Route.recorded() {
    get("/api/v1/persons") {
        val queryParamNamePart = getQueryParamNamePart(getQueryParamsAsSet(call.parameters))
        call.respondContents(
            "/<SOME_PATH>/src/main/resources/responses/recorded/get_api_v1_persons${queryParamNamePart}.json",
            ContentType.Application.Json
        )
    }
}
```

To use this in Mocker register this method in Application.kt like so:

```
mock { 
    recorded()
}
```

After the mocks are recorded, they can be moved/renamed and checked-in to version control if desired. 
