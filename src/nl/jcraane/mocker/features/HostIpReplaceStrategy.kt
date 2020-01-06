package nl.jcraane.mocker.features

interface HostIpReplaceStrategy {
    fun getHostIp(): String
}

class StaticHostIpReplacementStrategy(private val staticValue: String) : HostIpReplaceStrategy {
    override fun getHostIp() = staticValue
}