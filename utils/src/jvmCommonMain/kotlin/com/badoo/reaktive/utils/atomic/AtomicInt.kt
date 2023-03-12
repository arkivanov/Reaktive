package com.badoo.reaktive.utils.atomic

actual open class AtomicInt actual constructor(initialValue: Int) : java.util.concurrent.atomic.AtomicInteger(initialValue) {

    actual var value: Int
        get() = get()
        set(value) {
            set(value)
        }

    // See KT-16087
    override fun toByte(): Byte = value.toByte()
    override fun toChar(): Char = value.toChar()
    override fun toShort(): Short = value.toShort()
}
