package com.badoo.reaktive.utils.atomic

import kotlin.reflect.KProperty

inline fun <T> AtomicReference<T>.getAndChange(update: (T) -> T): T {
    var prev: T
    do {
        prev = value
    } while (!compareAndSet(prev, update(prev)))

    return prev
}

inline fun <T, R : T> AtomicReference<T>.changeAndGet(update: (T) -> R): R {
    var next: R
    do {
        val prev = value
        next = update(prev)
    } while (!compareAndSet(prev, next))

    return next
}

inline fun <T> AtomicReference<T>.change(update: (T) -> T) {
    getAndChange(update)
}

operator fun <T> AtomicReference<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value

operator fun <T> AtomicReference<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}
