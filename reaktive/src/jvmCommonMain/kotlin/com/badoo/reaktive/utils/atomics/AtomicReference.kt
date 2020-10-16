package com.badoo.reaktive.utils.atomics

@Suppress("ACTUAL_WITHOUT_EXPECT") // Workaround https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias AtomicReference<T> = java.util.concurrent.atomic.AtomicReference<T>

internal actual fun <T> AtomicReference<T>.getValue(): T = get()

internal actual fun <T> AtomicReference<T>.setValue(value: T) {
    set(value)
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") // The signature of the original method is same
internal actual fun <T> AtomicReference<T>.compareAndSet(expectedValue: T, newValue: T): Boolean =
    this.compareAndSet(expectedValue, newValue)

internal actual fun <T> atomic(initialValue: T): AtomicReference<T> = AtomicReference<T>(initialValue)
