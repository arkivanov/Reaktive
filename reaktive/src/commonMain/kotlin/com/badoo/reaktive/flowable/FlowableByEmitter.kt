package com.badoo.reaktive.flowable

import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper

fun <T> flowable(onSubscribe: (FlowableEmitter<T>) -> Unit): Flowable<T> =
    flowableSafe { observer ->
        val disposableWrapper = DisposableWrapper()
        observer.onSubscribe(disposableWrapper)
        val blockingCallbacks = observer.blocking()

        val emitter =
            object : FlowableEmitter<T>, CompletableCallbacks by blockingCallbacks {
                override fun onNext(value: T) {
                    blockingCallbacks.onNext(value)
                }

                override fun setDisposable(disposable: Disposable) {
                    disposableWrapper.set(disposable)
                }
            }

        try {
            onSubscribe(emitter)
        } catch (e: Throwable) {
            blockingCallbacks.onError(e)
        }
    }