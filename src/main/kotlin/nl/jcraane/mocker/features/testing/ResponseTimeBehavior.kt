package nl.jcraane.mocker.features.testing

sealed class ResponseTimeBehavior {
    abstract suspend fun delay()

    /**
     * Delays for a fixed amount of time.
     *
     * @param constant The time in milliseconds to delay.
     */
    class Fixed(private val constant: Long) : ResponseTimeBehavior() {
        override suspend fun delay() {
            kotlinx.coroutines.delay(constant)
        }

        override fun toString(): String {
            return "Fixed(constant=$constant)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Fixed

            if (constant != other.constant) return false

            return true
        }

        override fun hashCode(): Int {
            return constant.hashCode()
        }
    }

    /**
     * Delays for a random amount of time between the specified bounds, optionally increasing this random delay
     * with a constant value.
     *
     * @param variable The random delay is in this range.
     * @param constant Adds this delay on top of the calculated random delay.
     */
    class Random(private val variable: LongRange, private val constant: Long = 0L) : ResponseTimeBehavior() {
        override suspend fun delay() {
            kotlinx.coroutines.delay(
                constant + kotlin.random.Random.nextLong(variable.first, variable.last)
            )
        }

        override fun toString(): String {
            return "Random(variable=$variable, constant=$constant)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Random

            if (variable != other.variable) return false
            if (constant != other.constant) return false

            return true
        }

        override fun hashCode(): Int {
            var result = variable.hashCode()
            result = 31 * result + constant.hashCode()
            return result
        }
    }
}