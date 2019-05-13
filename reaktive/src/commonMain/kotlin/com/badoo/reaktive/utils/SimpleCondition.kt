package com.badoo.reaktive.utils

import com.badoo.reaktive.utils.atomicreference.AtomicReference

class SimpleCondition {

    private val lock = Lock()
    private val condition = lock.newCondition()
    private val flag = AtomicReference(false)

    fun await() {
        lock.synchronized {
            while (!flag.value) {
                condition.await()
            }
        }
    }

    fun signal() {
        lock.synchronized {
            flag.value = true
            condition.signal()
        }
    }

    fun reset() {
        flag.value = false
    }
}