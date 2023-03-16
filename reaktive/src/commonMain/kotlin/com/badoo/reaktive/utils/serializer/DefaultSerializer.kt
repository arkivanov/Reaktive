package com.badoo.reaktive.utils.serializer

internal inline fun <T, S> serializer(
    crossinline onValue: (value: T, token: S) -> Boolean,
): Serializer<T, S> =
    object : DefaultSerializer<T, S>() {
        override fun onValue(value: T, token: S): Boolean =
            onValue.invoke(value, token)
    }

internal inline fun <T> serializer(
    crossinline onValue: (value: T) -> Boolean,
): Serializer<T, Nothing?> =
    object : DefaultSerializer<T, Nothing?>() {
        override fun onValue(value: T, token: Nothing?): Boolean =
            onValue.invoke(value)
    }

internal abstract class DefaultSerializer<T, S> : AbstractSerializer<T, S>() {
    private var values: ArrayDeque<T>? = null
    private var tokens: ArrayDeque<S>? = null

    override fun addLast(value: T, token: S) {
        val values = values ?: ArrayDeque<T>().also { values = it }
        val tokens = tokens ?: ArrayDeque<S>().also { tokens = it }
        values.add(value)
        tokens.add(token)
    }

    override fun clearQueue() {
        values?.clear()
        tokens?.clear()
    }

    override fun isEmpty(): Boolean =
        values?.isEmpty() ?: true

    override fun removeFirstValue(): T =
        requireNotNull(values).removeFirst()

    override fun removeFirstToken(): S =
        requireNotNull(tokens).removeFirst()
}
