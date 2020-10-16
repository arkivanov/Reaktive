package com.badoo.reaktive.utils.atomics

import kotlin.reflect.KProperty

internal fun AtomicInt.addAndGet(value: Int): Int = changeAndGet { it + value }

internal inline fun AtomicInt.changeAndGet(update: (Int) -> Int): Int {
    var next: Int
    do {
        val prev = value
        next = update(prev)
    } while (!compareAndSet(prev, next))

    return next
}

internal operator fun AtomicInt.getValue(thisRef: Any?, property: KProperty<*>): Int = value

internal operator fun AtomicInt.setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
    this.value = value
}
