package com.badoo.reaktive.utils.serializer

import com.badoo.reaktive.utils.SynchronizedObject

/*
 * Derived from RxJava SerializedEmitter.
 */
internal abstract class AbstractSerializer<T> : SynchronizedObject(), Serializer<T> {

    private var isDraining = false

    protected abstract fun addLast(value: T)

    protected abstract fun clearQueue()

    protected abstract fun isEmpty(): Boolean

    protected abstract fun removeFirst(): T

    protected abstract fun onValue(value: T): Boolean

    override fun accept(value: T) {
        synchronized {
            if (isDraining) {
                addLast(value)
                return
            }

            isDraining = true
        }

        if (onValue(value)) {
            drainLoop()
        }
    }

    override fun clear() {
        synchronized(::clearQueue)
    }

    private fun drainLoop() {
        while (true) {
            val item =
                synchronized {
                    if (isEmpty()) {
                        isDraining = false
                        return
                    }

                    removeFirst()
                }

            if (!onValue(item)) {
                return
            }
        }
    }
}
