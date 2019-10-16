package com.badoo.reaktive.completable

import com.badoo.reaktive.disposable.Disposable

internal inline fun <D : Disposable> completableSafe(
    crossinline disposableFactory: () -> D,
    crossinline onSubscribe: (callbacks: CompletableCallbacks, disposable: D) -> Unit
): Completable =
    completableUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        val safeCallbacks = SafeCompletableCallbacks(observer, disposable)

        try {
            onSubscribe(safeCallbacks, disposable)
        } catch (e: Throwable) {
            safeCallbacks.onError(e)
        }
    }

internal open class SafeCompletableCallbacks(
    private val delegate: CompletableCallbacks,
    private val disposable: Disposable
) : CompletableCallbacks {
    override fun onComplete() {
        if (!disposable.isDisposed) {
            delegate.onComplete()
            disposable.dispose()
        }
    }

    override fun onError(error: Throwable) {
        if (!disposable.isDisposed) {
            delegate.onError(error)
            disposable.dispose()
        }
    }
}
