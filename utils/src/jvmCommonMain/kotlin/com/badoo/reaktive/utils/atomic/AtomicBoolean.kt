package com.badoo.reaktive.utils.atomic

actual open class AtomicBoolean actual constructor(initialValue: Boolean) : java.util.concurrent.atomic.AtomicBoolean(initialValue) {

    actual var value: Boolean
        get() = get()
        set(value) {
            set(value)
        }
}
