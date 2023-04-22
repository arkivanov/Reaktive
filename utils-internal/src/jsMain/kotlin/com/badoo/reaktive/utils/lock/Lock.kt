package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.InternalReaktiveApi

@InternalReaktiveApi
actual open class Lock {

    @Suppress("MemberNameEqualsClassName") // Matches expect class
    actual fun lock() {
        // no-op
    }

    actual fun unlock() {
        // no-op
    }

    actual fun newCondition(): Condition {
        error("Condition is not supported in JavaScript")
    }
}
