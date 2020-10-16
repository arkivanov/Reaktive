package com.badoo.reaktive.utils.atomic

internal expect class AtomicInt(initialValue: Int = 0) {

    var value: Int

    fun addAndGet(delta: Int): Int

    fun compareAndSet(expectedValue: Int, newValue: Int): Boolean
}
