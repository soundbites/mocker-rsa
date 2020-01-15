package nl.jcraane.mocker.util

import java.time.LocalDateTime

interface TimeProvider {
    val localDate: LocalDateTime
}

class RealTimeProvider : TimeProvider {
    override val localDate: LocalDateTime
        get() = LocalDateTime.now() // We do not care about timezone
}

class MockTimeProvider(private val dateTime: LocalDateTime) : TimeProvider {
    override val localDate: LocalDateTime
        get() = dateTime
}