package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.InternalReaktiveApi

@InternalReaktiveApi
expect interface Condition {

    fun await()

    fun awaitNanos(nanos: Long): Long

    fun signalAll()
}
