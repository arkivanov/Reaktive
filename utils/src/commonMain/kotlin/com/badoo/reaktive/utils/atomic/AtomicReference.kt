package com.badoo.reaktive.utils.atomic

internal expect class AtomicReference<T>(initialValue: T) {

    var value: T

    fun compareAndSet(expectedValue: T, newValue: T): Boolean
}
