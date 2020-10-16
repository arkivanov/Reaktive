package com.badoo.reaktive.utils

import com.badoo.reaktive.utils.atomics.atomic
import com.badoo.reaktive.utils.atomics.changeAndGet

internal class RefCounter(
    private val destroy: () -> Unit
) {

    private val count = atomic(1)

    fun retain(): Boolean =
        count.changeAndGet { if (it > 0) it + 1 else 0 } > 0

    fun release() {
        val newCount =
            count.changeAndGet {
                check(it > 0) { "RefCounter is already destroyed" }
                it - 1
            }

        if (newCount == 0) {
            destroy()
        }
    }
}
