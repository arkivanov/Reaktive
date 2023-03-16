package com.badoo.reaktive.utils.serializer

import com.badoo.reaktive.utils.SynchronizedObject
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet

/*
 * Derived from RxJava SerializedEmitter.
 */
internal abstract class AbstractSerializer<T, S> : SynchronizedObject(), Serializer<T, S> {

    private val counter = atomic(0)

    protected abstract fun addLast(value: T, token: S)

    protected abstract fun clearQueue()

    protected abstract fun isEmpty(): Boolean

    protected abstract fun removeFirstValue(): T

    protected abstract fun removeFirstToken(): S

    protected abstract fun onValue(value: T, token: S): Boolean

    override fun accept(value: T, token: S) {
        if (counter.compareAndSet(0, 1)) {
            if (!onValue(value, token)) {
                counter.value = -1
                return
            }

            if (counter.addAndGet(-1) == 0) {
                return
            }
        } else {
            if (counter.value < 0) {
                return
            }

            synchronized {
                addLast(value, token)
            }

            if (counter.updateAndGet { if (it >= 0) it + 1 else it } != 1) {
                return
            }
        }

        drainLoop()
    }

    override fun clear() {
        synchronized(::clearQueue)
    }

    private fun drainLoop() {
        var missed = 1
        while (true) {
            while (true) {
                var isEmpty = false
                var value: T? = null
                var token: S? = null

                synchronized {
                    isEmpty = isEmpty()
                    if (!isEmpty) {
                        value = removeFirstValue()
                        token = removeFirstToken()
                    }
                }

                if (isEmpty) {
                    break
                }

                @Suppress("UNCHECKED_CAST")
                if (!onValue(value as T, token as S)) {
                    counter.value = -1
                    return
                }
            }

            missed = counter.addAndGet(-missed)
            if (missed == 0) {
                break
            }
        }
    }
}
