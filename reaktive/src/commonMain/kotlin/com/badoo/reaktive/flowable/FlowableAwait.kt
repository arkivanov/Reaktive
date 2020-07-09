package com.badoo.reaktive.flowable

import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.lock.Lock
import com.badoo.reaktive.utils.lock.synchronized

interface FlowableAwait {

    fun await()
}

interface FlowableValue<out T> {

    val value: T

    fun notify()
}

inline fun <T> FlowableValue<T>.consume(block: (T) -> Unit) {
    try {
        block(value)
    } finally {
        notify()
    }
}

class FlowableAwaitNotify<T> : FlowableAwait, FlowableValue<T> {

    private val ref = AtomicReference<Any?>(Empty)
    private val lock = Lock()
    private val condition = lock.newCondition()

    override fun await() {
        lock.synchronized {
            while (ref.value !== Empty) {
                condition.await()
            }
        }
    }

    override var value: T
        get() = ref.value as T
        set(value) {
            ref.value = value
        }

    override fun notify() {
        lock.synchronized {
            ref.value = Empty
            condition.signal()
        }
    }

    fun destroy() {
        condition.destroy()
        lock.destroy()
    }

    private object Empty
}

// interface FlowableAwait : FlowableAwait, FlowableValue
