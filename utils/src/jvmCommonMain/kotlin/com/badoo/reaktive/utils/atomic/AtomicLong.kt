package com.badoo.reaktive.utils.atomic

actual open class AtomicLong actual constructor(initialValue: Long) : java.util.concurrent.atomic.AtomicLong(initialValue) {

    actual var value: Long
        get() = get()
        set(value) {
            set(value)
        }

    // See KT-16087
    override fun toByte(): Byte = value.toByte()
    override fun toChar(): Char = value.toChar()
    override fun toShort(): Short = value.toShort()
}
