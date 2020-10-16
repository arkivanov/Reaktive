package com.badoo.reaktive.utils.atomics

@Suppress("ACTUAL_WITHOUT_EXPECT") // Workaround https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias AtomicInt = java.util.concurrent.atomic.AtomicInteger

internal actual var AtomicInt.value: Int
    get() = get()
    set(value) {
        set(value)
    }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") // The signature of the original method is same
internal actual fun AtomicInt.compareAndSet(expectedValue: Int, newValue: Int): Boolean = compareAndSet(expectedValue, newValue)

internal actual fun atomic(value: Int): AtomicInt = AtomicInt(value)
