package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.SimpleCondition
import com.badoo.reaktive.utils.use

fun <T> FlowableObserver<T>.safe(): FlowableObserver<T> =
    object : FlowableObserver<T> {
        private lateinit var disposable: Disposable

        override fun onSubscribe(disposable: Disposable) {
            this.disposable = disposable
            this@safe.onSubscribe(disposable)
        }

        override fun onNext(value: FlowableValue<T>) {
            if (!disposable.isDisposed) {
                this@safe.onNext(value)
            } else {
                value.onProcessed()
            }
        }

        override fun onComplete() {
            if (!disposable.isDisposed) {
                try {
                    this@safe.onComplete()
                } finally {
                    disposable.dispose()
                }
            }
        }

        override fun onError(error: Throwable) {
            if (!disposable.isDisposed) {
                try {
                    this@safe.onError(error)
                } finally {
                    disposable.dispose()
                }
            }
        }
    }

fun <T> FlowableObserver<T>.onNextBlocking(value: T) {
    SimpleCondition()
        .use { condition ->
            onNext(FlowableValue(value, condition::signal))
            condition.await()
        }
}