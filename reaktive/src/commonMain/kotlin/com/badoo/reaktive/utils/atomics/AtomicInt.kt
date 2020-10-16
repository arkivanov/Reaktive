package com.badoo.reaktive.utils.atomics

internal expect class AtomicInt

internal expect var AtomicInt.value: Int

internal expect fun AtomicInt.compareAndSet(expectedValue: Int, newValue: Int): Boolean

internal expect fun atomic(value: Int): AtomicInt
