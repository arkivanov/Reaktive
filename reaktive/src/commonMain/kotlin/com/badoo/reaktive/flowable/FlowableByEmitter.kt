package com.badoo.reaktive.flowable

import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper

fun <T> flowable(onSubscribe: (FlowableEmitter<T>) -> Unit): Flowable<T> =
    flowableSafe {
        val disposableWrapper = DisposableWrapper()
        it.onSubscribe(disposableWrapper)
        val callbacks = BlockingFlowableCallbacks(it)

        val emitter =
            object : FlowableEmitter<T>, CompletableCallbacks by callbacks {
                override fun onNext(value: T) {
                    callbacks.onNext(value)
                }

                override fun setDisposable(disposable: Disposable) {
                    disposableWrapper.set(disposable)
                }
            }

        try {
            onSubscribe(emitter)
        } catch (e: Throwable) {
            callbacks.onError(e)
        }
    }