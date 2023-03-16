package com.badoo.reaktive.utils

internal expect class CountDownLatch(count: Int) {

    fun countDown()

    fun await()
}
