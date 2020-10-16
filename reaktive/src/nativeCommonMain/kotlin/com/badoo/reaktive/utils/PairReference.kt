package com.badoo.reaktive.utils

import com.badoo.reaktive.utils.atomics.setValue
import kotlin.native.concurrent.FreezableAtomicReference

internal actual open class PairReference<T, R> actual constructor(firstInitial: T, secondInitial: R) {

    private val _first = Freezableatomic(firstInitial)

    actual var first: T
        get() = _first.value
        set(value) {
            _first.setValue(value)
        }

    private val _second = Freezableatomic(secondInitial)

    actual var second: R
        get() = _second.value
        set(value) {
            _second.setValue(value)
        }
}
