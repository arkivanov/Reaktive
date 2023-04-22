package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.InternalReaktiveApi

@InternalReaktiveApi
inline fun Condition.waitFor(timeoutNanos: Long, predicate: () -> Boolean): Boolean {
    require(timeoutNanos >= 0L) { "Timeout must be not be negative" }

    var remainingNanos = timeoutNanos

    while (true) {
        if (predicate()) {
            return true
        }
        if (remainingNanos <= 0L) {
            return false
        }

        remainingNanos = awaitNanos(remainingNanos)
    }
}
