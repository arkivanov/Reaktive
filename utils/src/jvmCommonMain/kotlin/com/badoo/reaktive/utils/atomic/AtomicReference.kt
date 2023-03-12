package com.badoo.reaktive.utils.atomic

actual open class AtomicReference<T> actual constructor(initialValue: T) : java.util.concurrent.atomic.AtomicReference<T>(initialValue) {

    actual var value: T
        get() = get()
        set(value) {
            set(value)
        }
}
