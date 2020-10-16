package com.badoo.reaktive.utils.atomics

internal expect class AtomicBoolean

internal expect var AtomicBoolean.value: Boolean

internal expect fun AtomicBoolean.compareAndSet(expectedValue: Boolean, newValue: Boolean): Boolean

internal expect fun atomic(value: Boolean): AtomicBoolean
