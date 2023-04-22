package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.InternalReaktiveApi
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

@InternalReaktiveApi
actual open class ConditionLock : ReentrantLock() {

    private val condition = super.newCondition()

    actual fun await(timeout: Duration): Duration =
        condition.awaitNanos(timeout.inWholeNanoseconds).nanoseconds

    actual fun signal() {
        condition.signalAll()
    }
}
