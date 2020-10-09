package com.badoo.reaktive.utils.serializer

import com.badoo.reaktive.synchronized
import com.badoo.reaktive.utils.queue.Queue

internal abstract class SerializerImpl<in T>(queue: Queue<T>) : Serializer<T> {

    private var queue: Queue<T>? = queue
    private val monitor = Any()
    private var isDraining = false

    override fun accept(value: T) {
        val queueToDrain =
            monitor.synchronized {
                val queue = queue ?: return

                if (isDraining) {
                    queue.offer(value)
                    return
                }

                isDraining = true
                queue
            }

        if (!processValue(value)) {
            return
        }

        queueToDrain.drain()
    }

    override fun clear() {
        monitor.synchronized {
            queue?.clear()
        }
    }

    private fun Queue<T>.drain() {
        while (true) {
            val value =
                monitor.synchronized {
                    if (isEmpty) {
                        onDrainFinished(false)
                        return
                    }

                    @Suppress("UNCHECKED_CAST")
                    poll() as T
                }

            if (!processValue(value)) {
                return
            }
        }
    }

    private fun processValue(value: T): Boolean {
        if (!onValue(value)) {
            monitor.synchronized {
                onDrainFinished(true)
            }
            return false
        }

        return true
    }

    private fun onDrainFinished(terminate: Boolean) {
        isDraining = false
        if (terminate) {
            queue = null
        }
    }

    protected abstract fun onValue(value: T): Boolean
}
