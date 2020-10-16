package com.badoo.reaktive.utils.serializer

import com.badoo.reaktive.utils.atomics.AtomicBoolean
import com.badoo.reaktive.utils.atomics.AtomicInt
import com.badoo.reaktive.utils.atomics.AtomicReference
import com.badoo.reaktive.utils.atomics.getAndChange
import com.badoo.reaktive.utils.atomics.change
import com.badoo.reaktive.utils.plusSorted

/*
 * Derived from RxJava SerializedEmitter
 */
internal abstract class SerializerImpl<in T>(
    private val comparator: Comparator<in T>? = null
) : Serializer<T> {

    private val queue = AtomicReference<List<T>>(emptyList())
    private val isDone = AtomicBoolean()
    private val counter = atomic()

    override fun accept(value: T) {
        if (isDone.value) {
            return
        }

        if (counter.compareAndSet(0, 1)) {
            if (!onValue(value)) {
                isDone.value = true
                return
            }

            if (counter.addAndGet(-1) == 0) {
                return
            }
        } else {
            queue.change { it.addAndSort(value, comparator) }

            if (counter.addAndGet(1) > 1) {
                return
            }
        }

        drainLoop()
    }

    override fun clear() {
        queue.value = emptyList()
    }

    abstract fun onValue(value: T): Boolean

    private fun drainLoop() {
        var missed = 1
        while (true) {
            while (true) {
                val oldQueue = queue.getAndChange { it.drop(1) }

                if (oldQueue.isEmpty()) {
                    break
                }

                if (!onValue(oldQueue[0])) {
                    isDone.value = true
                    return
                }
            }

            missed = counter.addAndGet(-missed)
            if (missed == 0) {
                break
            }
        }
    }

    private companion object {
        private fun <T> List<T>.addAndSort(item: T, comparator: Comparator<in T>?): List<T> =
            if (comparator == null) plus(item) else plusSorted(item, comparator)
    }
}
