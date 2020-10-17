package com.badoo.reaktive.utils.locks

internal inline fun <T> Lock.synchronized(block: (Lock) -> T): T {
    acquire()

    return try {
        block(this)
    } finally {
        release()
    }
}

internal inline fun <T> Lock.use(block: (Lock) -> T): T =
    try {
        block(this)
    } finally {
        destroy()
    }

