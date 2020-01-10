package nl.jcraane.mocker.features.testing

import io.ktor.http.HttpMethod
import org.junit.Assert
import org.junit.Test

class ChaosMockerFeatureTest {
    @Test
    fun testBasicMatchIncomingRequests() {
        val config = ChaosMockerFeature.Configuration()
        config.slowResponseTimes.add(RequestConfig.get("/person"), ResponseTimeBehavior.Fixed(1500L))
        val feature = ChaosMockerFeature(config)
        Assert.assertNotNull(feature.findBestMatch(HttpMethod.Get, "/person", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNull(feature.findBestMatch(HttpMethod.Post, "/person", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNull(feature.findBestMatch(HttpMethod.Get, "/tasks", config.slowResponseTimes.getResponseTimesConfig()))
    }

    @Test
    fun testMatchSubPaths() {
        val config = ChaosMockerFeature.Configuration()
        config.slowResponseTimes.add(RequestConfig.post("/api/v1/actions/add"), ResponseTimeBehavior.Fixed(1500L))
        val feature = ChaosMockerFeature(config)
        Assert.assertNotNull(feature.findBestMatch(HttpMethod.Post,"/api/v1/actions/add", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNull(feature.findBestMatch(HttpMethod.Get,"/api/v1/actions/add", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNull(feature.findBestMatch(HttpMethod.Get,"/api/v1/actions", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNull(feature.findBestMatch(HttpMethod.Post,"/api/v1/actions", config.slowResponseTimes.getResponseTimesConfig()))

        val config2 = ChaosMockerFeature.Configuration()
        config2.slowResponseTimes.add(RequestConfig.post("api/v1/actions/add"), ResponseTimeBehavior.Fixed(1500L))
        Assert.assertNotNull(ChaosMockerFeature(config2).findBestMatch(HttpMethod.Post, "/api/v1/actions/add", config2.slowResponseTimes.getResponseTimesConfig()))

        val config3 = ChaosMockerFeature.Configuration()
        config3.slowResponseTimes.add(RequestConfig.post("/api/v1/actions/add"), ResponseTimeBehavior.Fixed(1500L))
        Assert.assertNotNull(ChaosMockerFeature(config3).findBestMatch(HttpMethod.Post, "api/v1/actions/add", config3.slowResponseTimes.getResponseTimesConfig()))
    }

    @Test
    fun testMatchWildcards() {
        val configWildcardPost = ChaosMockerFeature.Configuration()
        configWildcardPost.slowResponseTimes.add(RequestConfig.post("/api/v1/actions/*"), ResponseTimeBehavior.Fixed(1500L))
        val feature = ChaosMockerFeature(configWildcardPost)
        Assert.assertNotNull(feature.findBestMatch(HttpMethod.Post, "/api/v1/actions/add", configWildcardPost.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNotNull(feature.findBestMatch(HttpMethod.Post, "/api/v1/actions/addAnother", configWildcardPost.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNull(feature.findBestMatch(HttpMethod.Delete, "/api/v1/actions/delete", configWildcardPost.slowResponseTimes.getResponseTimesConfig()))

        val configWildcardAll = ChaosMockerFeature.Configuration()
        configWildcardAll.slowResponseTimes.add(RequestConfig.all("/api/v1/actions/*"), ResponseTimeBehavior.Fixed(1500L))
        val feature2 = ChaosMockerFeature(configWildcardAll)
        Assert.assertNotNull(feature2.findBestMatch(HttpMethod.Post, "/api/v1/actions/add", configWildcardAll.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNotNull(feature2.findBestMatch(HttpMethod.Post, "/api/v1/actions/addAnother", configWildcardAll.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNotNull(feature2.findBestMatch(HttpMethod.Delete, "/api/v1/actions/delete", configWildcardAll.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNotNull(feature2.findBestMatch(HttpMethod.Patch, "/api/v1/actions/patch", configWildcardAll.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNotNull(feature2.findBestMatch(HttpMethod.Put, "/api/v1/actions/put", configWildcardAll.slowResponseTimes.getResponseTimesConfig()))
    }

    @Test
    fun testMatchWildcards_2() {
        val config = ChaosMockerFeature.Configuration()
        config.slowResponseTimes.add(RequestConfig.get("/api/v1/**"), ResponseTimeBehavior.Fixed(1500L))
        val feature = ChaosMockerFeature(config)
        Assert.assertNotNull(feature.findBestMatch(HttpMethod.Get, "/api/v1/actions/add", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNotNull(feature.findBestMatch(HttpMethod.Get, "/api/v1/add", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNotNull(feature.findBestMatch(HttpMethod.Get, "/api/v1/add/c/v/s", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertNull(feature.findBestMatch(HttpMethod.Delete, "/api/v1/add/c/v/s", config.slowResponseTimes.getResponseTimesConfig()))
    }

    @Test
    fun findBestMatchOutOfMany() {
        val config = ChaosMockerFeature.Configuration()
        config.slowResponseTimes.add(RequestConfig.all(RequestConfig.ALL_PATHS), ResponseTimeBehavior.Fixed(100L))
        config.slowResponseTimes.add(RequestConfig.get("/api/v1/persons"), ResponseTimeBehavior.Fixed(200L))
        config.slowResponseTimes.add(RequestConfig.get("/api/v1/tasks"), ResponseTimeBehavior.Fixed(300L))
        config.slowResponseTimes.add(RequestConfig.get("/api/v1/**"), ResponseTimeBehavior.Fixed(400L))
        config.slowResponseTimes.add(RequestConfig.get("/api/v1/persons/*/detail"), ResponseTimeBehavior.Fixed(500L))

        val feature = ChaosMockerFeature(config)
        Assert.assertEquals(ResponseTimeBehavior.Fixed(200L), feature.findBestMatch(HttpMethod.Get, "/api/v1/persons", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertEquals(ResponseTimeBehavior.Fixed(400L), feature.findBestMatch(HttpMethod.Get, "/api/v1/calendar", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertEquals(ResponseTimeBehavior.Fixed(500L), feature.findBestMatch(HttpMethod.Get, "/api/v1/persons/1/detail", config.slowResponseTimes.getResponseTimesConfig()))
        Assert.assertEquals(ResponseTimeBehavior.Fixed(100L), feature.findBestMatch(HttpMethod.Get, "/api/v2/somethingelse", config.slowResponseTimes.getResponseTimesConfig()))
    }
}