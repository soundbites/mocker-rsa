package nl.jcraane.mocker.features.testing

sealed class ResponseTimeBehavior {
    abstract suspend fun delay()

    /**
     * Delays for a fixed amount of time.
     *
     * @param delay The time in milliseconds to delay.
     */
    class Fixed(private val delay: Long) : ResponseTimeBehavior() {
        override suspend fun delay() {
            kotlinx.coroutines.delay(delay)
        }
    }

    /**
     * Delays for a random amount of time between the specified bounds, optionally increasing this random delay
     * with a constant value.
     *
     * @param variableDelay The random delay is in this range.
     * @param constantDelay Adds this delay on top of the calculated random delay.
     */
    class Random(private val variableDelay: LongRange, private val constantDelay: Long = 0L) : ResponseTimeBehavior() {
        override suspend fun delay() {
            kotlinx.coroutines.delay(
                constantDelay + kotlin.random.Random.nextLong(variableDelay.first, variableDelay.last)
            )
        }
    }
}