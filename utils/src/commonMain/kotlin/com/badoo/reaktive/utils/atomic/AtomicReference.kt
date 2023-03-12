package com.badoo.reaktive.utils.atomic

expect open class AtomicReference<T>(initialValue: T) {

    var value: T

    fun compareAndSet(expectedValue: T, newValue: T): Boolean

    fun getAndSet(value: T): T
}
