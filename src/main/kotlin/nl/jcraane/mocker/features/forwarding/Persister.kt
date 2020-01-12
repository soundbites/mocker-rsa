package nl.jcraane.mocker.features.forwarding

interface Persister {
    fun persist(recorder: Recorder)
}