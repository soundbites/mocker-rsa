package nl.jcraane.mocker.features.forwarding

class WarningPersister : Persister {
    override fun persist(recorder: Recorder) {
        throw IllegalStateException("When recording is enabled, make sure a Persister is configured.")
    }
}