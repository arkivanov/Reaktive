package com.badoo.reaktive.utils.atomics

internal expect class AtomicReference<T>

// Workaround https://youtrack.jetbrains.com/issue/KT-41006
internal expect fun <T> AtomicReference<T>.getValue(): T

// Workaround https://youtrack.jetbrains.com/issue/KT-41006
internal expect fun <T> AtomicReference<T>.setValue(value: T)

internal expect fun <T> AtomicReference<T>.compareAndSet(expectedValue: T, newValue: T): Boolean

internal expect fun <T> atomic(initialValue: T): AtomicReference<T>
