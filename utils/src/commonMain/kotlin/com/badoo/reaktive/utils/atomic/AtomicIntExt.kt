package com.badoo.reaktive.utils.atomic

import kotlin.reflect.KProperty

internal inline fun AtomicInt.updateAndGet(update: (Int) -> Int): Int {
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
