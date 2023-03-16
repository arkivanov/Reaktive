package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.CountDownLatch
import kotlinx.atomicfu.atomic

/**
 * Blocks current thread until the current `Maybe` succeeds with a value (which is returned),
 * completes (`null` is returned) or fails with an exception (which is propagated).
 *
 * ⚠️ Please note that this method is not available in JavaScript due to its single threaded nature.
 * A runtime exception will be thrown when this method is called in JavaScript. If you need this
 * in JavaScript for testing purposes, then consider using `Single.testAwait()` extension
 * from the `reaktive-testing` module.
 */
fun <T> Maybe<T>.blockingGet(): T? {
    val latch = CountDownLatch(1)

    var successResult: T? = null
    var errorResult: Throwable? = null

    val observer =
        object : MaybeObserver<T> {
            val disposableRef = atomic<Disposable?>(null)

            override fun onSubscribe(disposable: Disposable) {
                disposableRef.value = disposable
            }

            override fun onSuccess(value: T) {
                successResult = value
                latch.countDown()
            }

            override fun onComplete() {
                latch.countDown()
            }

            override fun onError(error: Throwable) {
                errorResult = error
                latch.countDown()
            }
        }

    subscribe(observer)

    try {
        latch.await()
    } catch (e: Throwable) {
        observer.disposableRef.value?.dispose()
        throw e
    }

    errorResult?.also {
        throw it
    }

    return successResult
}
