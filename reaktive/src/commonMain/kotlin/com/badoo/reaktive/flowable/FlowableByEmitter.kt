package com.badoo.reaktive.flowable

import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper

fun <T> flowable(onSubscribe: (FlowableEmitter<T>) -> Unit): Flowable<T> =
    flowableSafe { observer ->
        val disposableWrapper = DisposableWrapper()
        observer.onSubscribe(disposableWrapper)

        val emitter =
            object : FlowableEmitter<T>, CompletableCallbacks by observer {
                override fun onNext(value: T) {
                    observer.onNextBlocking(value)
                }

                override fun setDisposable(disposable: Disposable) {
                    disposableWrapper.set(disposable)
                }
            }

        try {
            onSubscribe(emitter)
        } catch (e: Throwable) {
            observer.onError(e)
        }
    }