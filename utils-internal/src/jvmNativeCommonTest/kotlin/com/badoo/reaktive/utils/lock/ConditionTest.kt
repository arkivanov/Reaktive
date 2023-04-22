package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.NANOS_IN_MILLI
import com.badoo.reaktive.utils.NANOS_IN_SECOND
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.test.doInBackground
import kotlin.test.Test
import kotlin.test.assertTrue

class ConditionTest {

    @Test
    fun awaitNanos_returns_non_positive_result_WHEN_timeout_reached() {
        val lock = Lock()
        val condition = lock.newCondition()

        val result =
            lock.synchronized {
                condition.awaitNanos(NANOS_IN_MILLI)
            }

        assertTrue(result <= 0L)
    }

    @Test
    fun awaitNanos_returns_positive_result_less_than_timeout_WHEN_signalled_before_timeout() {
        val lock = Lock()
        val condition = lock.newCondition()
        val timeoutNanos = 5L * NANOS_IN_SECOND
        val isReady = AtomicBoolean()

        doInBackground {
            while (!isReady.value) {
                // no-op
            }

            lock.synchronized {
                condition.signalAll()
            }
        }

        val result =
            lock.synchronized {
                isReady.value = true
                condition.awaitNanos(timeoutNanos)
            }

        assertTrue((result > 0L) && (result < timeoutNanos))
    }
}
