package nl.jcraane.mocker.features

import io.ktor.application.ApplicationCall
import io.ktor.request.userAgent

interface HostIpReplaceStrategy {
    fun getHostIp(call: ApplicationCall? = null): String
}

class StaticHostIpReplacementStrategy(private val staticValue: String = "localhost") : HostIpReplaceStrategy {
    override fun getHostIp(call: ApplicationCall?) = staticValue
}

class UserAgentHostIpReplacementStrategy(private val mapping: Map<String, String>) : HostIpReplaceStrategy {
    override fun getHostIp(call: ApplicationCall?) : String {
        val value = mapping
            .filter { entry -> call?.request?.userAgent()?.contains(entry.key, ignoreCase = true) == true }
            .map { it.value}
            .firstOrNull()
        return value ?: ""
    }
}