package com.badoo.reaktive.utils.serializer

import com.badoo.reaktive.utils.queue.PriorityQueue

internal inline fun <T> serializer(
    comparator: Comparator<in T>,
    crossinline onValue: (T) -> Boolean,
): Serializer<T, Nothing?> =
    object : PrioritySerializer<T>(comparator) {
        override fun onValue(value: T, token: Nothing?): Boolean =
            onValue.invoke(value)
    }

internal abstract class PrioritySerializer<T>(
    private val comparator: Comparator<in T>,
) : AbstractSerializer<T, Nothing?>() {

    private var queue: PriorityQueue<T>? = null

    override fun addLast(value: T, token: Nothing?) {
        val queue = queue ?: PriorityQueue(comparator).also { queue = it }
        queue.offer(value)
    }

    override fun clearQueue() {
        queue?.clear()
    }

    override fun isEmpty(): Boolean =
        queue?.isEmpty ?: true

    @Suppress("UNCHECKED_CAST")
    override fun removeFirstValue(): T =
        requireNotNull(queue).poll() as T

    override fun removeFirstToken(): Nothing? =
        null
}
