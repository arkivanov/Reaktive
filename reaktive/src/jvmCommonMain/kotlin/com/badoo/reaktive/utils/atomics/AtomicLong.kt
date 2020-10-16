package com.badoo.reaktive.utils.atomics

@Suppress("ACTUAL_WITHOUT_EXPECT") // Workaround https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias AtomicLong = java.util.concurrent.atomic.AtomicLong

internal actual var AtomicLong.value: Long
    get() = get()
    set(value) {
        set(value)
    }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal actual inline fun AtomicLong.compareAndSet(expectedValue: Long, newValue: Long): Boolean = compareAndSet(expectedValue, newValue)

internal actual inline fun atomic(value: Long): AtomicLong = AtomicLong(value)
