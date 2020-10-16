package com.badoo.reaktive.utils.atomics

@Suppress("ACTUAL_WITHOUT_EXPECT") // Workaround https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias AtomicBoolean = java.util.concurrent.atomic.AtomicBoolean

internal actual var AtomicBoolean.value: Boolean
    get() = get()
    set(value) {
        set(value)
    }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal actual fun AtomicBoolean.compareAndSet(expectedValue: Boolean, newValue: Boolean): Boolean = compareAndSet(expectedValue, newValue)

internal actual fun atomic(value: Boolean): AtomicBoolean = AtomicBoolean(value)
