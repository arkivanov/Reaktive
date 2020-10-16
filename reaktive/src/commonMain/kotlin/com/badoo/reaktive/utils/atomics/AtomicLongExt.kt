package com.badoo.reaktive.utils.atomics

import kotlin.reflect.KProperty

internal inline fun AtomicLong.addAndGet(value: Long): Long = changeAndGet { it + value }

internal inline fun AtomicLong.changeAndGet(update: (Long) -> Long): Long {
    var next: Long
    do {
        val prev = value
        next = update(prev)
    } while (!compareAndSet(prev, next))

    return next
}

internal operator fun AtomicLong.getValue(thisRef: Any?, property: KProperty<*>): Long = value

internal operator fun AtomicLong.setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
    this.value = value
}
