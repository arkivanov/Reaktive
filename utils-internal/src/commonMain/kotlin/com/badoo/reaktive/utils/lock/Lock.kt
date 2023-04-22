package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.InternalReaktiveApi

@Suppress("EmptyDefaultConstructor")
@InternalReaktiveApi
expect open class Lock() {

    @Suppress("MemberNameEqualsClassName") // Matches java.util.concurrent.locks.ReentrantLock
    fun lock()

    fun unlock()

    /**
     * Returns a [Condition] instance for use with this `Lock` instance.
     *
     * ⚠️ Please note that this method is not available in JavaScript due to its single threaded nature.
     * A runtime exception will be thrown when this method is called in JavaScript.
     */
    fun newCondition(): Condition
}
