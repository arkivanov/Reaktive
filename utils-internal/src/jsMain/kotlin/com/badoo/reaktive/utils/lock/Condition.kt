package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.InternalReaktiveApi

@InternalReaktiveApi
actual interface Condition {

    actual fun await()

    actual fun awaitNanos(nanos: Long): Long

    actual fun signalAll()
}
