package com.badoo.reaktive.utils.atomics

internal expect class AtomicLong

internal expect var AtomicLong.value: Long

internal expect fun AtomicLong.compareAndSet(expectedValue: Long, newValue: Long): Boolean

internal expect fun atomic(value: Long): AtomicLong
