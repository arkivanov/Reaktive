package com.badoo.reaktive.completable

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper

fun completable(onSubscribe: (emitter: CompletableEmitter) -> Unit): Completable =
    completableUnsafe { observer ->
        val disposableWrapper = DisposableWrapper()
        observer.onSubscribe(disposableWrapper)

        val emitter =
            object : SafeCompletableCallbacks(observer, disposableWrapper), CompletableEmitter {
                override val isDisposed: Boolean get() = disposableWrapper.isDisposed

                override fun setDisposable(disposable: Disposable) {
                    disposableWrapper.set(disposable)
                }
            }

        try {
            onSubscribe(emitter)
        } catch (e: Throwable) {
            emitter.onError(e)
        }
    }