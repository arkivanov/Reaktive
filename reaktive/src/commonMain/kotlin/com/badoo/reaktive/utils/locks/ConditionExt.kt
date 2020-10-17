package com.badoo.reaktive.utils.locks

internal inline fun <T> Condition.use(block: (Condition) -> T): T =
    try {
        block(this)
    } finally {
        destroy()
    }
